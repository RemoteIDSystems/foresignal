// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushPayloadCWProxy {
  PushPayload v(PushPayloadVEnum v);

  PushPayload event(PushPayloadEventEnum event);

  PushPayload id(int? id);

  PushPayload alertId(int? alertId);

  PushPayload ruleId(String? ruleId);

  PushPayload severity(PushPayloadSeverityEnum? severity);

  PushPayload title(String? title);

  PushPayload shortTitle(String? shortTitle);

  PushPayload body(String? body);

  PushPayload url(String? url);

  PushPayload links(PushPayloadLinks? links);

  PushPayload tags(List<String>? tags);

  PushPayload lang(String? lang);

  PushPayload tag(String? tag);

  PushPayload silent(bool? silent);

  PushPayload ts(int? ts);

  PushPayload actions(List<PushPayloadActionsInner>? actions);

  PushPayload confirmUrl(String? confirmUrl);

  PushPayload subscriptionId(String? subscriptionId);

  PushPayload code(String? code);

  PushPayload exercise(bool? exercise);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  PushPayload call({
    PushPayloadVEnum v,
    PushPayloadEventEnum event,
    int? id,
    int? alertId,
    String? ruleId,
    PushPayloadSeverityEnum? severity,
    String? title,
    String? shortTitle,
    String? body,
    String? url,
    PushPayloadLinks? links,
    List<String>? tags,
    String? lang,
    String? tag,
    bool? silent,
    int? ts,
    List<PushPayloadActionsInner>? actions,
    String? confirmUrl,
    String? subscriptionId,
    String? code,
    bool? exercise,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushPayload.copyWith(...)` or call `instanceOfPushPayload.copyWith.fieldName(value)` for a single field.
class _$PushPayloadCWProxyImpl implements _$PushPayloadCWProxy {
  const _$PushPayloadCWProxyImpl(this._value);

  final PushPayload _value;

  @override
  PushPayload v(PushPayloadVEnum v) => call(v: v);

  @override
  PushPayload event(PushPayloadEventEnum event) => call(event: event);

  @override
  PushPayload id(int? id) => call(id: id);

  @override
  PushPayload alertId(int? alertId) => call(alertId: alertId);

  @override
  PushPayload ruleId(String? ruleId) => call(ruleId: ruleId);

  @override
  PushPayload severity(PushPayloadSeverityEnum? severity) =>
      call(severity: severity);

  @override
  PushPayload title(String? title) => call(title: title);

  @override
  PushPayload shortTitle(String? shortTitle) => call(shortTitle: shortTitle);

  @override
  PushPayload body(String? body) => call(body: body);

  @override
  PushPayload url(String? url) => call(url: url);

  @override
  PushPayload links(PushPayloadLinks? links) => call(links: links);

  @override
  PushPayload tags(List<String>? tags) => call(tags: tags);

  @override
  PushPayload lang(String? lang) => call(lang: lang);

  @override
  PushPayload tag(String? tag) => call(tag: tag);

  @override
  PushPayload silent(bool? silent) => call(silent: silent);

  @override
  PushPayload ts(int? ts) => call(ts: ts);

  @override
  PushPayload actions(List<PushPayloadActionsInner>? actions) =>
      call(actions: actions);

  @override
  PushPayload confirmUrl(String? confirmUrl) => call(confirmUrl: confirmUrl);

  @override
  PushPayload subscriptionId(String? subscriptionId) =>
      call(subscriptionId: subscriptionId);

  @override
  PushPayload code(String? code) => call(code: code);

  @override
  PushPayload exercise(bool? exercise) => call(exercise: exercise);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushPayload call({
    Object? v = const $CopyWithPlaceholder(),
    Object? event = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? alertId = const $CopyWithPlaceholder(),
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? shortTitle = const $CopyWithPlaceholder(),
    Object? body = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? lang = const $CopyWithPlaceholder(),
    Object? tag = const $CopyWithPlaceholder(),
    Object? silent = const $CopyWithPlaceholder(),
    Object? ts = const $CopyWithPlaceholder(),
    Object? actions = const $CopyWithPlaceholder(),
    Object? confirmUrl = const $CopyWithPlaceholder(),
    Object? subscriptionId = const $CopyWithPlaceholder(),
    Object? code = const $CopyWithPlaceholder(),
    Object? exercise = const $CopyWithPlaceholder(),
  }) {
    return PushPayload(
      v: v == const $CopyWithPlaceholder() || v == null
          ? _value.v
          // ignore: cast_nullable_to_non_nullable
          : v as PushPayloadVEnum,
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as PushPayloadEventEnum,
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int?,
      alertId: alertId == const $CopyWithPlaceholder()
          ? _value.alertId
          // ignore: cast_nullable_to_non_nullable
          : alertId as int?,
      ruleId: ruleId == const $CopyWithPlaceholder()
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String?,
      severity: severity == const $CopyWithPlaceholder()
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as PushPayloadSeverityEnum?,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      shortTitle: shortTitle == const $CopyWithPlaceholder()
          ? _value.shortTitle
          // ignore: cast_nullable_to_non_nullable
          : shortTitle as String?,
      body: body == const $CopyWithPlaceholder()
          ? _value.body
          // ignore: cast_nullable_to_non_nullable
          : body as String?,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as PushPayloadLinks?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      lang: lang == const $CopyWithPlaceholder()
          ? _value.lang
          // ignore: cast_nullable_to_non_nullable
          : lang as String?,
      tag: tag == const $CopyWithPlaceholder()
          ? _value.tag
          // ignore: cast_nullable_to_non_nullable
          : tag as String?,
      silent: silent == const $CopyWithPlaceholder()
          ? _value.silent
          // ignore: cast_nullable_to_non_nullable
          : silent as bool?,
      ts: ts == const $CopyWithPlaceholder()
          ? _value.ts
          // ignore: cast_nullable_to_non_nullable
          : ts as int?,
      actions: actions == const $CopyWithPlaceholder()
          ? _value.actions
          // ignore: cast_nullable_to_non_nullable
          : actions as List<PushPayloadActionsInner>?,
      confirmUrl: confirmUrl == const $CopyWithPlaceholder()
          ? _value.confirmUrl
          // ignore: cast_nullable_to_non_nullable
          : confirmUrl as String?,
      subscriptionId: subscriptionId == const $CopyWithPlaceholder()
          ? _value.subscriptionId
          // ignore: cast_nullable_to_non_nullable
          : subscriptionId as String?,
      code: code == const $CopyWithPlaceholder()
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as String?,
      exercise: exercise == const $CopyWithPlaceholder()
          ? _value.exercise
          // ignore: cast_nullable_to_non_nullable
          : exercise as bool?,
    );
  }
}

extension $PushPayloadCopyWith on PushPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushPayload.copyWith(...)` or `instanceOfPushPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushPayloadCWProxy get copyWith => _$PushPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushPayload _$PushPayloadFromJson(Map<String, dynamic> json) => $checkedCreate(
  'PushPayload',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['v', 'event']);
    final val = PushPayload(
      v: $checkedConvert('v', (v) => $enumDecode(_$PushPayloadVEnumEnumMap, v)),
      event: $checkedConvert(
        'event',
        (v) => $enumDecode(_$PushPayloadEventEnumEnumMap, v),
      ),
      id: $checkedConvert('id', (v) => (v as num?)?.toInt()),
      alertId: $checkedConvert('alertId', (v) => (v as num?)?.toInt()),
      ruleId: $checkedConvert('ruleId', (v) => v as String?),
      severity: $checkedConvert(
        'severity',
        (v) => $enumDecodeNullable(_$PushPayloadSeverityEnumEnumMap, v),
      ),
      title: $checkedConvert('title', (v) => v as String?),
      shortTitle: $checkedConvert('shortTitle', (v) => v as String?),
      body: $checkedConvert('body', (v) => v as String?),
      url: $checkedConvert('url', (v) => v as String?),
      links: $checkedConvert(
        'links',
        (v) => v == null
            ? null
            : PushPayloadLinks.fromJson(v as Map<String, dynamic>),
      ),
      tags: $checkedConvert(
        'tags',
        (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
      ),
      lang: $checkedConvert('lang', (v) => v as String?),
      tag: $checkedConvert('tag', (v) => v as String?),
      silent: $checkedConvert('silent', (v) => v as bool?),
      ts: $checkedConvert('ts', (v) => (v as num?)?.toInt()),
      actions: $checkedConvert(
        'actions',
        (v) => (v as List<dynamic>?)
            ?.map(
              (e) =>
                  PushPayloadActionsInner.fromJson(e as Map<String, dynamic>),
            )
            .toList(),
      ),
      confirmUrl: $checkedConvert('confirmUrl', (v) => v as String?),
      subscriptionId: $checkedConvert('subscriptionId', (v) => v as String?),
      code: $checkedConvert('code', (v) => v as String?),
      exercise: $checkedConvert('exercise', (v) => v as bool? ?? false),
    );
    return val;
  },
);

Map<String, dynamic> _$PushPayloadToJson(PushPayload instance) =>
    <String, dynamic>{
      'v': _$PushPayloadVEnumEnumMap[instance.v]!,
      'event': _$PushPayloadEventEnumEnumMap[instance.event]!,
      'id': ?instance.id,
      'alertId': ?instance.alertId,
      'ruleId': ?instance.ruleId,
      'severity': ?_$PushPayloadSeverityEnumEnumMap[instance.severity],
      'title': ?instance.title,
      'shortTitle': ?instance.shortTitle,
      'body': ?instance.body,
      'url': ?instance.url,
      'links': ?instance.links?.toJson(),
      'tags': ?instance.tags,
      'lang': ?instance.lang,
      'tag': ?instance.tag,
      'silent': ?instance.silent,
      'ts': ?instance.ts,
      'actions': ?instance.actions?.map((e) => e.toJson()).toList(),
      'confirmUrl': ?instance.confirmUrl,
      'subscriptionId': ?instance.subscriptionId,
      'code': ?instance.code,
      'exercise': ?instance.exercise,
    };

const _$PushPayloadVEnumEnumMap = {PushPayloadVEnum.number1: 1};

const _$PushPayloadEventEnumEnumMap = {
  PushPayloadEventEnum.alert: 'alert',
  PushPayloadEventEnum.briefing: 'briefing',
  PushPayloadEventEnum.system: 'system',
  PushPayloadEventEnum.verify: 'verify',
};

const _$PushPayloadSeverityEnumEnumMap = {
  PushPayloadSeverityEnum.info: 'info',
  PushPayloadSeverityEnum.warning: 'warning',
  PushPayloadSeverityEnum.critical: 'critical',
};
