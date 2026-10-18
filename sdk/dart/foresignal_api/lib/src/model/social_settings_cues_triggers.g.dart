// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_settings_cues_triggers.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialSettingsCuesTriggersCWProxy {
  SocialSettingsCuesTriggers situationActivated(bool? situationActivated);

  SocialSettingsCuesTriggers situationEscalated(bool? situationEscalated);

  SocialSettingsCuesTriggers findingTypes(List<String>? findingTypes);

  SocialSettingsCuesTriggers anomalies(bool? anomalies);

  SocialSettingsCuesTriggers criticalAlerts(bool? criticalAlerts);

  SocialSettingsCuesTriggers canonicalFamilies(List<String>? canonicalFamilies);

  SocialSettingsCuesTriggers minEvidenceGroups(int? minEvidenceGroups);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsCuesTriggers(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsCuesTriggers(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialSettingsCuesTriggers call({
    bool? situationActivated,
    bool? situationEscalated,
    List<String>? findingTypes,
    bool? anomalies,
    bool? criticalAlerts,
    List<String>? canonicalFamilies,
    int? minEvidenceGroups,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialSettingsCuesTriggers.copyWith(...)` or call `instanceOfSocialSettingsCuesTriggers.copyWith.fieldName(value)` for a single field.
class _$SocialSettingsCuesTriggersCWProxyImpl
    implements _$SocialSettingsCuesTriggersCWProxy {
  const _$SocialSettingsCuesTriggersCWProxyImpl(this._value);

  final SocialSettingsCuesTriggers _value;

  @override
  SocialSettingsCuesTriggers situationActivated(bool? situationActivated) =>
      call(situationActivated: situationActivated);

  @override
  SocialSettingsCuesTriggers situationEscalated(bool? situationEscalated) =>
      call(situationEscalated: situationEscalated);

  @override
  SocialSettingsCuesTriggers findingTypes(List<String>? findingTypes) =>
      call(findingTypes: findingTypes);

  @override
  SocialSettingsCuesTriggers anomalies(bool? anomalies) =>
      call(anomalies: anomalies);

  @override
  SocialSettingsCuesTriggers criticalAlerts(bool? criticalAlerts) =>
      call(criticalAlerts: criticalAlerts);

  @override
  SocialSettingsCuesTriggers canonicalFamilies(
    List<String>? canonicalFamilies,
  ) => call(canonicalFamilies: canonicalFamilies);

  @override
  SocialSettingsCuesTriggers minEvidenceGroups(int? minEvidenceGroups) =>
      call(minEvidenceGroups: minEvidenceGroups);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsCuesTriggers(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsCuesTriggers(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialSettingsCuesTriggers call({
    Object? situationActivated = const $CopyWithPlaceholder(),
    Object? situationEscalated = const $CopyWithPlaceholder(),
    Object? findingTypes = const $CopyWithPlaceholder(),
    Object? anomalies = const $CopyWithPlaceholder(),
    Object? criticalAlerts = const $CopyWithPlaceholder(),
    Object? canonicalFamilies = const $CopyWithPlaceholder(),
    Object? minEvidenceGroups = const $CopyWithPlaceholder(),
  }) {
    return SocialSettingsCuesTriggers(
      situationActivated: situationActivated == const $CopyWithPlaceholder()
          ? _value.situationActivated
          // ignore: cast_nullable_to_non_nullable
          : situationActivated as bool?,
      situationEscalated: situationEscalated == const $CopyWithPlaceholder()
          ? _value.situationEscalated
          // ignore: cast_nullable_to_non_nullable
          : situationEscalated as bool?,
      findingTypes: findingTypes == const $CopyWithPlaceholder()
          ? _value.findingTypes
          // ignore: cast_nullable_to_non_nullable
          : findingTypes as List<String>?,
      anomalies: anomalies == const $CopyWithPlaceholder()
          ? _value.anomalies
          // ignore: cast_nullable_to_non_nullable
          : anomalies as bool?,
      criticalAlerts: criticalAlerts == const $CopyWithPlaceholder()
          ? _value.criticalAlerts
          // ignore: cast_nullable_to_non_nullable
          : criticalAlerts as bool?,
      canonicalFamilies: canonicalFamilies == const $CopyWithPlaceholder()
          ? _value.canonicalFamilies
          // ignore: cast_nullable_to_non_nullable
          : canonicalFamilies as List<String>?,
      minEvidenceGroups: minEvidenceGroups == const $CopyWithPlaceholder()
          ? _value.minEvidenceGroups
          // ignore: cast_nullable_to_non_nullable
          : minEvidenceGroups as int?,
    );
  }
}

extension $SocialSettingsCuesTriggersCopyWith on SocialSettingsCuesTriggers {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialSettingsCuesTriggers.copyWith(...)` or `instanceOfSocialSettingsCuesTriggers.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialSettingsCuesTriggersCWProxy get copyWith =>
      _$SocialSettingsCuesTriggersCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialSettingsCuesTriggers _$SocialSettingsCuesTriggersFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialSettingsCuesTriggers', json, ($checkedConvert) {
  final val = SocialSettingsCuesTriggers(
    situationActivated: $checkedConvert(
      'situationActivated',
      (v) => v as bool? ?? true,
    ),
    situationEscalated: $checkedConvert(
      'situationEscalated',
      (v) => v as bool? ?? true,
    ),
    findingTypes: $checkedConvert(
      'findingTypes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    anomalies: $checkedConvert('anomalies', (v) => v as bool? ?? true),
    criticalAlerts: $checkedConvert(
      'criticalAlerts',
      (v) => v as bool? ?? true,
    ),
    canonicalFamilies: $checkedConvert(
      'canonicalFamilies',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    minEvidenceGroups: $checkedConvert(
      'minEvidenceGroups',
      (v) => (v as num?)?.toInt() ?? 2,
    ),
  );
  return val;
});

Map<String, dynamic> _$SocialSettingsCuesTriggersToJson(
  SocialSettingsCuesTriggers instance,
) => <String, dynamic>{
  'situationActivated': ?instance.situationActivated,
  'situationEscalated': ?instance.situationEscalated,
  'findingTypes': ?instance.findingTypes,
  'anomalies': ?instance.anomalies,
  'criticalAlerts': ?instance.criticalAlerts,
  'canonicalFamilies': ?instance.canonicalFamilies,
  'minEvidenceGroups': ?instance.minEvidenceGroups,
};
