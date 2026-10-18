package ai.foresignal.report;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collab.AccessAction;
import ai.foresignal.collab.AccessResolver;
import ai.foresignal.collab.AclRole;
import ai.foresignal.collab.CollabInputs;
import ai.foresignal.collab.GroupService;
import ai.foresignal.collab.MeEvents;
import ai.foresignal.collab.ObjectAclEntity;
import ai.foresignal.collab.ObjectAclRepository;
import ai.foresignal.collab.PersonalNotifier;
import ai.foresignal.collab.PrincipalNotificationEntity;
import ai.foresignal.collab.PrincipalNotificationRepository;
import ai.foresignal.collab.PrincipalViews;
import ai.foresignal.collab.ShareService;
import ai.foresignal.collab.ShareableObjectEntity;
import ai.foresignal.collab.Visibility;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.security.Tlp;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;

/**
 * Sends a published report to principals and groups on this instance (prompt 132, {@code sendReport}).
 *
 * <p>Every recipient is checked against the report's TLP label first (130 rules; one refusal refuses the send with
 * {@code 403 TLP_VIOLATION} naming the rule). Then each grantee gets the {@code viewer} role (a stronger existing
 * role is kept and a {@code private} report becomes {@code shared}), every principal reached (group members expanded,
 * the sender left out) gets a {@code report.received} inbox item and {@code me} event, delivered to their own channels
 * by their 130 preferences, and the {@code channels} named in the request (which must be recipients' own channels)
 * receive a message with the link to the HTML rendition. The send is recorded, so a retraction reaches the same
 * people.
 */
@Service
public class ReportSender {

    /** The members of {@code ReportSendInput}. */
    static final Set<String> FIELDS = Set.of("principals", "groups", "channels", "message", "revision");

    private final ReportService reports;
    private final ReportPublisher publisher;
    private final ReportSendRepository sends;
    private final AccessResolver access;
    private final ShareService shares;
    private final ObjectAclRepository acls;
    private final GroupService groups;
    private final PersonalNotifier notifier;
    private final PrincipalNotificationRepository preferences;
    private final NotificationService notifications;
    private final PrincipalViews views;
    private final Clock clock;
    private final TransactionTemplate tx;

    /** Creates the sender. */
    public ReportSender(ReportService reports, ReportPublisher publisher, ReportSendRepository sends,
            AccessResolver access, ShareService shares, ObjectAclRepository acls, GroupService groups,
            PersonalNotifier notifier, PrincipalNotificationRepository preferences, NotificationService notifications,
            PrincipalViews views, Clock clock, PlatformTransactionManager transactions) {
        this.reports = reports;
        this.publisher = publisher;
        this.sends = sends;
        this.access = access;
        this.shares = shares;
        this.acls = acls;
        this.groups = groups;
        this.notifier = notifier;
        this.preferences = preferences;
        this.notifications = notifications;
        this.views = views;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactions);
    }

    /**
     * Sends a report ({@code sendReport}; owner or editor).
     *
     * @param id the report
     * @param body the {@code ReportSendInput}
     * @param caller the sender
     * @return the recorded send
     */
    public ReportSendEntity send(String id, JsonNode body, ObjectCaller caller) {
        ReportEntity r = reports.require(id, caller, AccessAction.SHARE);
        CollabInputs.object(body, "ReportSendInput", FIELDS);
        Integer number = body.hasNonNull("revision") ? body.get("revision").asInt() : null;
        ReportRevisionEntity rev = publisher.revision(r, number);
        if (ReportVocabulary.RETRACTED.equals(rev.getStatus())) {
            throw ReportPublisher.notPublished(r, "revision " + rev.getRevision() + " is retracted");
        }
        List<String> grantees = new ArrayList<>();
        for (String p : CollabInputs.strings(body, "principals", ShareService.MAX_GRANTEES)) {
            String g = shares.grantee(p.startsWith("principal:") ? p : "principal:" + p);
            if (!grantees.contains(g)) {
                grantees.add(g);
            }
        }
        for (String g : CollabInputs.strings(body, "groups", ShareService.MAX_GRANTEES)) {
            String grantee = shares.grantee(g.startsWith("group:") ? g : "group:" + g);
            if (!grantees.contains(grantee)) {
                grantees.add(grantee);
            }
        }
        if (grantees.isEmpty()) {
            throw ValidationException.forField("principals", "name at least one principal or group");
        }
        String message = CollabInputs.text(body, "message", 0, 2000);
        ShareableObjectEntity rec = reports.record(r);
        Tlp tlp = Tlp.fromWire(rec.getTlp());
        for (String g : grantees) {
            shares.checkTlp(tlp, rec.getOwner(), List.of(g), Visibility.SHARED);
        }
        Map<String, Set<String>> reached = new LinkedHashMap<>();
        Set<String> people = new LinkedHashSet<>();
        for (String g : grantees) {
            Set<String> ps = new LinkedHashSet<>();
            if (g.startsWith("principal:")) {
                ps.add(g.substring("principal:".length()));
            } else {
                ps.addAll(groups.memberIds(g.substring("group:".length())));
            }
            ps.remove(caller.principal());
            reached.put(g, ps);
            people.addAll(ps);
        }
        Map<UUID, String> channelOwners = channels(CollabInputs.strings(body, "channels", 50), people);
        Instant now = clock.instant();
        return tx.execute(s -> {
            for (String g : grantees) {
                ObjectAclEntity row = acls.findByObjectRefAndGrantee(rec.getObjectRef(), g).orElse(null);
                if (row == null) {
                    acls.save(new ObjectAclEntity(Ulids.next(now), rec.getObjectRef(), g, AclRole.VIEWER,
                            caller.principal(), now));
                }
            }
            if (rec.getVisibility() == Visibility.PRIVATE) {
                rec.setVisibility(Visibility.SHARED);
                access.save(rec);
            }
            Map<String, Object> from = views.summary(caller.principal());
            String who = from == null ? "Someone" : String.valueOf(from.get("displayName"));
            String ref = ReportService.ref(r.getId());
            for (String p : people) {
                notifier.send(new PersonalNotifier.Notice(p, MeEvents.REPORT_RECEIVED, who + " sent you "
                        + r.getDisplayId() + " " + r.getTitle(), message, ref, caller.principal(), rec.getTlp(),
                        MeEvents.report(ref, r.getTitle(), rec.getTlp(), rev.getRevision(), from, message, null, now)));
            }
            List<Map<String, Object>> outcomes = new ArrayList<>();
            for (String g : grantees) {
                List<String> channelIds = new ArrayList<>();
                for (Map.Entry<UUID, String> c : channelOwners.entrySet()) {
                    if (reached.get(g).contains(c.getValue())) {
                        channelIds.add(c.getKey().toString());
                    }
                }
                Map<String, Object> outcome = new LinkedHashMap<>();
                outcome.put("grantee", g);
                outcome.put("status", channelIds.isEmpty() ? (reached.get(g).isEmpty() ? "granted" : "notified")
                        : "delivered");
                outcome.put("channelIds", channelIds);
                outcomes.add(outcome);
            }
            for (Map.Entry<UUID, String> c : channelOwners.entrySet()) {
                deliver(c.getKey(), r, rev, rec.getTlp(), who, message);
            }
            ReportSendEntity send = new ReportSendEntity(Ulids.next(now), r.getId(), rev.getRevision(),
                    caller.principal(), now);
            send.setMessage(message);
            send.setRecipients(outcomes);
            send.setPrincipalIds(new ArrayList<>(people));
            return sends.save(send);
        });
    }

    /**
     * Checks the requested channels: each must be a personal channel of a recipient (130 preferences).
     *
     * @return channel id → owning principal
     */
    Map<UUID, String> channels(List<String> ids, Set<String> people) {
        Map<UUID, String> owned = new LinkedHashMap<>();
        for (String p : people) {
            for (PrincipalNotificationEntity pref : preferences.findByPrincipalIdOrderByCreatedAtAscIdAsc(p)) {
                if (pref.isEnabled() && pref.getChannelId() != null) {
                    owned.put(pref.getChannelId(), p);
                }
            }
        }
        Map<UUID, String> out = new LinkedHashMap<>();
        for (String id : ids) {
            UUID uuid;
            try {
                uuid = UUID.fromString(id.strip());
            } catch (IllegalArgumentException e) {
                throw ValidationException.forField("channels", "'" + id + "' is not a channel id");
            }
            String owner = owned.get(uuid);
            if (owner == null) {
                throw ValidationException.forField("channels", "channel " + id + " is not an enabled personal"
                        + " channel of a recipient");
            }
            out.put(uuid, owner);
        }
        return out;
    }

    private void deliver(UUID channelId, ReportEntity r, ReportRevisionEntity rev, String tlp, String who,
            String message) {
        String label = "[TLP:" + tlp + "] ";
        String link = "/v1/reports/" + r.getId() + "/rendition?format=html&revision=" + rev.getRevision();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ref", ReportService.ref(r.getId()));
        data.put("revision", rev.getRevision());
        data.put("tlp", tlp);
        StringBuilder body = new StringBuilder(label).append(who).append(" sent ").append(r.getDisplayId())
                .append(" revision ").append(rev.getRevision()).append(". Read it at ").append(link).append('.');
        if (message != null && !message.isBlank()) {
            body.append("\n\n").append(message);
        }
        NotificationMessage m = NotificationMessage.builder(ChannelService.PERSONAL_EVENT, MeEvents.REPORT_RECEIVED,
                label + r.getDisplayId() + " " + r.getTitle(), "info").body(body.toString()).url(link).data(data)
                .build();
        notifications.publishPersonal(m, channelId);
    }

    /** The {@code ReportSend} view. */
    public Map<String, Object> view(ReportSendEntity s) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", s.getId());
        out.put("reportId", s.getReportId());
        out.put("revision", s.getRevision());
        out.put("sentBy", reports.summary(s.getSentBy(), PrincipalViews.cache()));
        out.put("sentAt", s.getSentAt().toString());
        out.put("message", s.getMessage());
        out.put("recipients", s.getRecipients());
        return out;
    }
}
