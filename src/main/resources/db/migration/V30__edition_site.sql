-- Hosted and shareable edition site (prompt NP-005). Additive only.
--
-- briefing.site_enabled / site_shareable: BriefingInput.site {enabled, shareable} (NP-001); both false for every
-- existing briefing, so nothing is served until an owner turns the site on.
-- edition_site_key: the edition's own path keys, so its site URLs stay stable: kind 'feed' is a 035 feed key whose only
-- prefix is /v1/editions/{id}/site, kind 'share' a 112 share link of target type edition-site. The plaintext key is
-- kept AES-256-GCM encrypted (CredentialCipher) because EditionSite returns the URLs on every read; key_id names the
-- api_token or share_link row, which is revoked when the site (or sharing) is turned off or the edition deleted.
alter table briefing add column site_enabled boolean not null default false;
alter table briefing add column site_shareable boolean not null default false;

create table edition_site_key (
    briefing_id uuid not null,
    kind varchar(8) not null,
    key_id varchar(64) not null,
    key_ciphertext bytea not null,
    key_nonce bytea not null,
    expires_at timestamp(6) with time zone not null,
    created_at timestamp(6) with time zone not null,
    primary key (briefing_id, kind),
    constraint fk_edition_site_key_briefing foreign key (briefing_id) references briefing (id) on delete cascade,
    constraint edition_site_key_kind check (kind in ('feed', 'share'))
);
