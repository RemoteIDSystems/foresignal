// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationPatchCWProxy {
  SituationPatch title(String? title);

  SituationPatch theme(SituationTheme? theme);

  SituationPatch severityOverride(Severity? severityOverride);

  SituationPatch status(SituationPatchStatusEnum? status);

  SituationPatch topicIds(List<String>? topicIds);

  SituationPatch entityIds(List<String>? entityIds);

  SituationPatch investigationIds(List<String>? investigationIds);

  SituationPatch reason(String? reason);

  SituationPatch version(int? version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationPatch call({
    String? title,
    SituationTheme? theme,
    Severity? severityOverride,
    SituationPatchStatusEnum? status,
    List<String>? topicIds,
    List<String>? entityIds,
    List<String>? investigationIds,
    String? reason,
    int? version,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationPatch.copyWith(...)` or call `instanceOfSituationPatch.copyWith.fieldName(value)` for a single field.
class _$SituationPatchCWProxyImpl implements _$SituationPatchCWProxy {
  const _$SituationPatchCWProxyImpl(this._value);

  final SituationPatch _value;

  @override
  SituationPatch title(String? title) => call(title: title);

  @override
  SituationPatch theme(SituationTheme? theme) => call(theme: theme);

  @override
  SituationPatch severityOverride(Severity? severityOverride) =>
      call(severityOverride: severityOverride);

  @override
  SituationPatch status(SituationPatchStatusEnum? status) =>
      call(status: status);

  @override
  SituationPatch topicIds(List<String>? topicIds) => call(topicIds: topicIds);

  @override
  SituationPatch entityIds(List<String>? entityIds) =>
      call(entityIds: entityIds);

  @override
  SituationPatch investigationIds(List<String>? investigationIds) =>
      call(investigationIds: investigationIds);

  @override
  SituationPatch reason(String? reason) => call(reason: reason);

  @override
  SituationPatch version(int? version) => call(version: version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationPatch call({
    Object? title = const $CopyWithPlaceholder(),
    Object? theme = const $CopyWithPlaceholder(),
    Object? severityOverride = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? topicIds = const $CopyWithPlaceholder(),
    Object? entityIds = const $CopyWithPlaceholder(),
    Object? investigationIds = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
  }) {
    return SituationPatch(
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      theme: theme == const $CopyWithPlaceholder()
          ? _value.theme
          // ignore: cast_nullable_to_non_nullable
          : theme as SituationTheme?,
      severityOverride: severityOverride == const $CopyWithPlaceholder()
          ? _value.severityOverride
          // ignore: cast_nullable_to_non_nullable
          : severityOverride as Severity?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SituationPatchStatusEnum?,
      topicIds: topicIds == const $CopyWithPlaceholder()
          ? _value.topicIds
          // ignore: cast_nullable_to_non_nullable
          : topicIds as List<String>?,
      entityIds: entityIds == const $CopyWithPlaceholder()
          ? _value.entityIds
          // ignore: cast_nullable_to_non_nullable
          : entityIds as List<String>?,
      investigationIds: investigationIds == const $CopyWithPlaceholder()
          ? _value.investigationIds
          // ignore: cast_nullable_to_non_nullable
          : investigationIds as List<String>?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int?,
    );
  }
}

extension $SituationPatchCopyWith on SituationPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationPatch.copyWith(...)` or `instanceOfSituationPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationPatchCWProxy get copyWith => _$SituationPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationPatch _$SituationPatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SituationPatch', json, ($checkedConvert) {
      final val = SituationPatch(
        title: $checkedConvert('title', (v) => v as String?),
        theme: $checkedConvert(
          'theme',
          (v) => $enumDecodeNullable(_$SituationThemeEnumMap, v),
        ),
        severityOverride: $checkedConvert(
          'severityOverride',
          (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
        ),
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(_$SituationPatchStatusEnumEnumMap, v),
        ),
        topicIds: $checkedConvert(
          'topicIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        entityIds: $checkedConvert(
          'entityIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        investigationIds: $checkedConvert(
          'investigationIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        reason: $checkedConvert('reason', (v) => v as String?),
        version: $checkedConvert('version', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$SituationPatchToJson(SituationPatch instance) =>
    <String, dynamic>{
      'title': ?instance.title,
      'theme': ?_$SituationThemeEnumMap[instance.theme],
      'severityOverride': ?_$SeverityEnumMap[instance.severityOverride],
      'status': ?_$SituationPatchStatusEnumEnumMap[instance.status],
      'topicIds': ?instance.topicIds,
      'entityIds': ?instance.entityIds,
      'investigationIds': ?instance.investigationIds,
      'reason': ?instance.reason,
      'version': ?instance.version,
    };

const _$SituationThemeEnumMap = {
  SituationTheme.maritime: 'maritime',
  SituationTheme.aviation: 'aviation',
  SituationTheme.conflict: 'conflict',
  SituationTheme.infrastructure: 'infrastructure',
  SituationTheme.weather: 'weather',
  SituationTheme.cyber: 'cyber',
  SituationTheme.markets: 'markets',
  SituationTheme.health: 'health',
  SituationTheme.space: 'space',
  SituationTheme.mixed: 'mixed',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};

const _$SituationPatchStatusEnumEnumMap = {
  SituationPatchStatusEnum.active: 'active',
  SituationPatchStatusEnum.closed: 'closed',
};
