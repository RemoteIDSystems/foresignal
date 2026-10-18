// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_velocity_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialVelocityEventCWProxy {
  SocialVelocityEvent event(SocialVelocityEventEventEnum event);

  SocialVelocityEvent findingId(String findingId);

  SocialVelocityEvent scope(SocialVelocityEventScope scope);

  SocialVelocityEvent platform(String platform);

  SocialVelocityEvent language(String? language);

  SocialVelocityEvent velocityRatio(num velocityRatio);

  SocialVelocityEvent uniqueAuthors(int uniqueAuthors);

  SocialVelocityEvent zRobust(num? zRobust);

  SocialVelocityEvent severity(FindingSeverity severity);

  SocialVelocityEvent detectedAt(DateTime detectedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVelocityEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVelocityEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialVelocityEvent call({
    SocialVelocityEventEventEnum event,
    String findingId,
    SocialVelocityEventScope scope,
    String platform,
    String? language,
    num velocityRatio,
    int uniqueAuthors,
    num? zRobust,
    FindingSeverity severity,
    DateTime detectedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialVelocityEvent.copyWith(...)` or call `instanceOfSocialVelocityEvent.copyWith.fieldName(value)` for a single field.
class _$SocialVelocityEventCWProxyImpl implements _$SocialVelocityEventCWProxy {
  const _$SocialVelocityEventCWProxyImpl(this._value);

  final SocialVelocityEvent _value;

  @override
  SocialVelocityEvent event(SocialVelocityEventEventEnum event) =>
      call(event: event);

  @override
  SocialVelocityEvent findingId(String findingId) => call(findingId: findingId);

  @override
  SocialVelocityEvent scope(SocialVelocityEventScope scope) =>
      call(scope: scope);

  @override
  SocialVelocityEvent platform(String platform) => call(platform: platform);

  @override
  SocialVelocityEvent language(String? language) => call(language: language);

  @override
  SocialVelocityEvent velocityRatio(num velocityRatio) =>
      call(velocityRatio: velocityRatio);

  @override
  SocialVelocityEvent uniqueAuthors(int uniqueAuthors) =>
      call(uniqueAuthors: uniqueAuthors);

  @override
  SocialVelocityEvent zRobust(num? zRobust) => call(zRobust: zRobust);

  @override
  SocialVelocityEvent severity(FindingSeverity severity) =>
      call(severity: severity);

  @override
  SocialVelocityEvent detectedAt(DateTime detectedAt) =>
      call(detectedAt: detectedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVelocityEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVelocityEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialVelocityEvent call({
    Object? event = const $CopyWithPlaceholder(),
    Object? findingId = const $CopyWithPlaceholder(),
    Object? scope = const $CopyWithPlaceholder(),
    Object? platform = const $CopyWithPlaceholder(),
    Object? language = const $CopyWithPlaceholder(),
    Object? velocityRatio = const $CopyWithPlaceholder(),
    Object? uniqueAuthors = const $CopyWithPlaceholder(),
    Object? zRobust = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? detectedAt = const $CopyWithPlaceholder(),
  }) {
    return SocialVelocityEvent(
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as SocialVelocityEventEventEnum,
      findingId: findingId == const $CopyWithPlaceholder() || findingId == null
          ? _value.findingId
          // ignore: cast_nullable_to_non_nullable
          : findingId as String,
      scope: scope == const $CopyWithPlaceholder() || scope == null
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as SocialVelocityEventScope,
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as String,
      language: language == const $CopyWithPlaceholder()
          ? _value.language
          // ignore: cast_nullable_to_non_nullable
          : language as String?,
      velocityRatio:
          velocityRatio == const $CopyWithPlaceholder() || velocityRatio == null
          ? _value.velocityRatio
          // ignore: cast_nullable_to_non_nullable
          : velocityRatio as num,
      uniqueAuthors:
          uniqueAuthors == const $CopyWithPlaceholder() || uniqueAuthors == null
          ? _value.uniqueAuthors
          // ignore: cast_nullable_to_non_nullable
          : uniqueAuthors as int,
      zRobust: zRobust == const $CopyWithPlaceholder()
          ? _value.zRobust
          // ignore: cast_nullable_to_non_nullable
          : zRobust as num?,
      severity: severity == const $CopyWithPlaceholder() || severity == null
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as FindingSeverity,
      detectedAt:
          detectedAt == const $CopyWithPlaceholder() || detectedAt == null
          ? _value.detectedAt
          // ignore: cast_nullable_to_non_nullable
          : detectedAt as DateTime,
    );
  }
}

extension $SocialVelocityEventCopyWith on SocialVelocityEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialVelocityEvent.copyWith(...)` or `instanceOfSocialVelocityEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialVelocityEventCWProxy get copyWith =>
      _$SocialVelocityEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialVelocityEvent _$SocialVelocityEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialVelocityEvent', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'event',
          'findingId',
          'scope',
          'platform',
          'velocityRatio',
          'uniqueAuthors',
          'severity',
          'detectedAt',
        ],
      );
      final val = SocialVelocityEvent(
        event: $checkedConvert(
          'event',
          (v) => $enumDecode(_$SocialVelocityEventEventEnumEnumMap, v),
        ),
        findingId: $checkedConvert('findingId', (v) => v as String),
        scope: $checkedConvert(
          'scope',
          (v) => SocialVelocityEventScope.fromJson(v as Map<String, dynamic>),
        ),
        platform: $checkedConvert('platform', (v) => v as String),
        language: $checkedConvert('language', (v) => v as String?),
        velocityRatio: $checkedConvert('velocityRatio', (v) => v as num),
        uniqueAuthors: $checkedConvert(
          'uniqueAuthors',
          (v) => (v as num).toInt(),
        ),
        zRobust: $checkedConvert('zRobust', (v) => v as num?),
        severity: $checkedConvert(
          'severity',
          (v) => $enumDecode(_$FindingSeverityEnumMap, v),
        ),
        detectedAt: $checkedConvert(
          'detectedAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SocialVelocityEventToJson(
  SocialVelocityEvent instance,
) => <String, dynamic>{
  'event': _$SocialVelocityEventEventEnumEnumMap[instance.event]!,
  'findingId': instance.findingId,
  'scope': instance.scope.toJson(),
  'platform': instance.platform,
  'language': ?instance.language,
  'velocityRatio': instance.velocityRatio,
  'uniqueAuthors': instance.uniqueAuthors,
  'zRobust': ?instance.zRobust,
  'severity': _$FindingSeverityEnumMap[instance.severity]!,
  'detectedAt': instance.detectedAt.toIso8601String(),
};

const _$SocialVelocityEventEventEnumEnumMap = {
  SocialVelocityEventEventEnum.socialPeriodVelocity: 'social.velocity',
};

const _$FindingSeverityEnumMap = {
  FindingSeverity.info: 'info',
  FindingSeverity.notable: 'notable',
  FindingSeverity.significant: 'significant',
};
