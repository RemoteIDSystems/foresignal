// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationInputCWProxy {
  SituationInput title(String title);

  SituationInput theme(SituationTheme theme);

  SituationInput status(SituationInputStatusEnum? status);

  SituationInput severityOverride(Severity? severityOverride);

  SituationInput regionCodes(List<String>? regionCodes);

  SituationInput topicIds(List<String>? topicIds);

  SituationInput entityIds(List<String>? entityIds);

  SituationInput investigationIds(List<String>? investigationIds);

  SituationInput members(List<SituationMemberInput>? members);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationInput(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationInput call({
    String title,
    SituationTheme theme,
    SituationInputStatusEnum? status,
    Severity? severityOverride,
    List<String>? regionCodes,
    List<String>? topicIds,
    List<String>? entityIds,
    List<String>? investigationIds,
    List<SituationMemberInput>? members,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationInput.copyWith(...)` or call `instanceOfSituationInput.copyWith.fieldName(value)` for a single field.
class _$SituationInputCWProxyImpl implements _$SituationInputCWProxy {
  const _$SituationInputCWProxyImpl(this._value);

  final SituationInput _value;

  @override
  SituationInput title(String title) => call(title: title);

  @override
  SituationInput theme(SituationTheme theme) => call(theme: theme);

  @override
  SituationInput status(SituationInputStatusEnum? status) =>
      call(status: status);

  @override
  SituationInput severityOverride(Severity? severityOverride) =>
      call(severityOverride: severityOverride);

  @override
  SituationInput regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  SituationInput topicIds(List<String>? topicIds) => call(topicIds: topicIds);

  @override
  SituationInput entityIds(List<String>? entityIds) =>
      call(entityIds: entityIds);

  @override
  SituationInput investigationIds(List<String>? investigationIds) =>
      call(investigationIds: investigationIds);

  @override
  SituationInput members(List<SituationMemberInput>? members) =>
      call(members: members);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationInput call({
    Object? title = const $CopyWithPlaceholder(),
    Object? theme = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? severityOverride = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? topicIds = const $CopyWithPlaceholder(),
    Object? entityIds = const $CopyWithPlaceholder(),
    Object? investigationIds = const $CopyWithPlaceholder(),
    Object? members = const $CopyWithPlaceholder(),
  }) {
    return SituationInput(
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      theme: theme == const $CopyWithPlaceholder() || theme == null
          ? _value.theme
          // ignore: cast_nullable_to_non_nullable
          : theme as SituationTheme,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SituationInputStatusEnum?,
      severityOverride: severityOverride == const $CopyWithPlaceholder()
          ? _value.severityOverride
          // ignore: cast_nullable_to_non_nullable
          : severityOverride as Severity?,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
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
      members: members == const $CopyWithPlaceholder()
          ? _value.members
          // ignore: cast_nullable_to_non_nullable
          : members as List<SituationMemberInput>?,
    );
  }
}

extension $SituationInputCopyWith on SituationInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationInput.copyWith(...)` or `instanceOfSituationInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationInputCWProxy get copyWith => _$SituationInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationInput _$SituationInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SituationInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['title', 'theme']);
      final val = SituationInput(
        title: $checkedConvert('title', (v) => v as String),
        theme: $checkedConvert(
          'theme',
          (v) => $enumDecode(_$SituationThemeEnumMap, v),
        ),
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(_$SituationInputStatusEnumEnumMap, v),
        ),
        severityOverride: $checkedConvert(
          'severityOverride',
          (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
        ),
        regionCodes: $checkedConvert(
          'regionCodes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
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
        members: $checkedConvert(
          'members',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => SituationMemberInput.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SituationInputToJson(SituationInput instance) =>
    <String, dynamic>{
      'title': instance.title,
      'theme': _$SituationThemeEnumMap[instance.theme]!,
      'status': ?_$SituationInputStatusEnumEnumMap[instance.status],
      'severityOverride': ?_$SeverityEnumMap[instance.severityOverride],
      'regionCodes': ?instance.regionCodes,
      'topicIds': ?instance.topicIds,
      'entityIds': ?instance.entityIds,
      'investigationIds': ?instance.investigationIds,
      'members': ?instance.members?.map((e) => e.toJson()).toList(),
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

const _$SituationInputStatusEnumEnumMap = {
  SituationInputStatusEnum.emerging: 'emerging',
  SituationInputStatusEnum.active: 'active',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};
