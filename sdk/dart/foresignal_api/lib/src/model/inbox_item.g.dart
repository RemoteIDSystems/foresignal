// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'inbox_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InboxItemCWProxy {
  InboxItem id(int id);

  InboxItem event(String event);

  InboxItem category(InboxItemCategoryEnum category);

  InboxItem severity(Severity? severity);

  InboxItem title(String title);

  InboxItem body(String? body);

  InboxItem ref(String? ref);

  InboxItem envelope(ObjectEnvelope? envelope);

  InboxItem actor(PrincipalSummary? actor);

  InboxItem tlp(Tlp? tlp);

  InboxItem createdAt(DateTime createdAt);

  InboxItem read(bool read);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InboxItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InboxItem(...).copyWith(id: 12, name: "My name")
  /// ```
  InboxItem call({
    int id,
    String event,
    InboxItemCategoryEnum category,
    Severity? severity,
    String title,
    String? body,
    String? ref,
    ObjectEnvelope? envelope,
    PrincipalSummary? actor,
    Tlp? tlp,
    DateTime createdAt,
    bool read,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInboxItem.copyWith(...)` or call `instanceOfInboxItem.copyWith.fieldName(value)` for a single field.
class _$InboxItemCWProxyImpl implements _$InboxItemCWProxy {
  const _$InboxItemCWProxyImpl(this._value);

  final InboxItem _value;

  @override
  InboxItem id(int id) => call(id: id);

  @override
  InboxItem event(String event) => call(event: event);

  @override
  InboxItem category(InboxItemCategoryEnum category) =>
      call(category: category);

  @override
  InboxItem severity(Severity? severity) => call(severity: severity);

  @override
  InboxItem title(String title) => call(title: title);

  @override
  InboxItem body(String? body) => call(body: body);

  @override
  InboxItem ref(String? ref) => call(ref: ref);

  @override
  InboxItem envelope(ObjectEnvelope? envelope) => call(envelope: envelope);

  @override
  InboxItem actor(PrincipalSummary? actor) => call(actor: actor);

  @override
  InboxItem tlp(Tlp? tlp) => call(tlp: tlp);

  @override
  InboxItem createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  InboxItem read(bool read) => call(read: read);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InboxItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InboxItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InboxItem call({
    Object? id = const $CopyWithPlaceholder(),
    Object? event = const $CopyWithPlaceholder(),
    Object? category = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? body = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? envelope = const $CopyWithPlaceholder(),
    Object? actor = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? read = const $CopyWithPlaceholder(),
  }) {
    return InboxItem(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as String,
      category: category == const $CopyWithPlaceholder() || category == null
          ? _value.category
          // ignore: cast_nullable_to_non_nullable
          : category as InboxItemCategoryEnum,
      severity: severity == const $CopyWithPlaceholder()
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as Severity?,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      body: body == const $CopyWithPlaceholder()
          ? _value.body
          // ignore: cast_nullable_to_non_nullable
          : body as String?,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      envelope: envelope == const $CopyWithPlaceholder()
          ? _value.envelope
          // ignore: cast_nullable_to_non_nullable
          : envelope as ObjectEnvelope?,
      actor: actor == const $CopyWithPlaceholder()
          ? _value.actor
          // ignore: cast_nullable_to_non_nullable
          : actor as PrincipalSummary?,
      tlp: tlp == const $CopyWithPlaceholder()
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      read: read == const $CopyWithPlaceholder() || read == null
          ? _value.read
          // ignore: cast_nullable_to_non_nullable
          : read as bool,
    );
  }
}

extension $InboxItemCopyWith on InboxItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInboxItem.copyWith(...)` or `instanceOfInboxItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InboxItemCWProxy get copyWith => _$InboxItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InboxItem _$InboxItemFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('InboxItem', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'event',
      'category',
      'title',
      'createdAt',
      'read',
    ],
  );
  final val = InboxItem(
    id: $checkedConvert('id', (v) => (v as num).toInt()),
    event: $checkedConvert('event', (v) => v as String),
    category: $checkedConvert(
      'category',
      (v) => $enumDecode(_$InboxItemCategoryEnumEnumMap, v),
    ),
    severity: $checkedConvert(
      'severity',
      (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
    ),
    title: $checkedConvert('title', (v) => v as String),
    body: $checkedConvert('body', (v) => v as String?),
    ref: $checkedConvert('ref', (v) => v as String?),
    envelope: $checkedConvert(
      'envelope',
      (v) =>
          v == null ? null : ObjectEnvelope.fromJson(v as Map<String, dynamic>),
    ),
    actor: $checkedConvert(
      'actor',
      (v) => v == null
          ? null
          : PrincipalSummary.fromJson(v as Map<String, dynamic>),
    ),
    tlp: $checkedConvert('tlp', (v) => $enumDecodeNullable(_$TlpEnumMap, v)),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    read: $checkedConvert('read', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$InboxItemToJson(InboxItem instance) => <String, dynamic>{
  'id': instance.id,
  'event': instance.event,
  'category': _$InboxItemCategoryEnumEnumMap[instance.category]!,
  'severity': ?_$SeverityEnumMap[instance.severity],
  'title': instance.title,
  'body': ?instance.body,
  'ref': ?instance.ref,
  'envelope': ?instance.envelope?.toJson(),
  'actor': ?instance.actor?.toJson(),
  'tlp': ?_$TlpEnumMap[instance.tlp],
  'createdAt': instance.createdAt.toIso8601String(),
  'read': instance.read,
};

const _$InboxItemCategoryEnumEnumMap = {
  InboxItemCategoryEnum.mention: 'mention',
  InboxItemCategoryEnum.share: 'share',
  InboxItemCategoryEnum.report: 'report',
  InboxItemCategoryEnum.chronicle: 'chronicle',
  InboxItemCategoryEnum.assessment: 'assessment',
  InboxItemCategoryEnum.commentReply: 'comment-reply',
  InboxItemCategoryEnum.accessRequest: 'access-request',
  InboxItemCategoryEnum.savedSearch: 'saved-search',
  InboxItemCategoryEnum.system: 'system',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};
