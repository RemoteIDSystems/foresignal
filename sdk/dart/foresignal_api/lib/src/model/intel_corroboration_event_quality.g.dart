// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_corroboration_event_quality.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelCorroborationEventQualityCWProxy {
  IntelCorroborationEventQuality strongFamilies(int? strongFamilies);

  IntelCorroborationEventQuality strongGroups(int? strongGroups);

  IntelCorroborationEventQuality moderateFamilies(int? moderateFamilies);

  IntelCorroborationEventQuality requireModalities(int? requireModalities);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelCorroborationEventQuality(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelCorroborationEventQuality(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelCorroborationEventQuality call({
    int? strongFamilies,
    int? strongGroups,
    int? moderateFamilies,
    int? requireModalities,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelCorroborationEventQuality.copyWith(...)` or call `instanceOfIntelCorroborationEventQuality.copyWith.fieldName(value)` for a single field.
class _$IntelCorroborationEventQualityCWProxyImpl
    implements _$IntelCorroborationEventQualityCWProxy {
  const _$IntelCorroborationEventQualityCWProxyImpl(this._value);

  final IntelCorroborationEventQuality _value;

  @override
  IntelCorroborationEventQuality strongFamilies(int? strongFamilies) =>
      call(strongFamilies: strongFamilies);

  @override
  IntelCorroborationEventQuality strongGroups(int? strongGroups) =>
      call(strongGroups: strongGroups);

  @override
  IntelCorroborationEventQuality moderateFamilies(int? moderateFamilies) =>
      call(moderateFamilies: moderateFamilies);

  @override
  IntelCorroborationEventQuality requireModalities(int? requireModalities) =>
      call(requireModalities: requireModalities);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelCorroborationEventQuality(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelCorroborationEventQuality(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelCorroborationEventQuality call({
    Object? strongFamilies = const $CopyWithPlaceholder(),
    Object? strongGroups = const $CopyWithPlaceholder(),
    Object? moderateFamilies = const $CopyWithPlaceholder(),
    Object? requireModalities = const $CopyWithPlaceholder(),
  }) {
    return IntelCorroborationEventQuality(
      strongFamilies: strongFamilies == const $CopyWithPlaceholder()
          ? _value.strongFamilies
          // ignore: cast_nullable_to_non_nullable
          : strongFamilies as int?,
      strongGroups: strongGroups == const $CopyWithPlaceholder()
          ? _value.strongGroups
          // ignore: cast_nullable_to_non_nullable
          : strongGroups as int?,
      moderateFamilies: moderateFamilies == const $CopyWithPlaceholder()
          ? _value.moderateFamilies
          // ignore: cast_nullable_to_non_nullable
          : moderateFamilies as int?,
      requireModalities: requireModalities == const $CopyWithPlaceholder()
          ? _value.requireModalities
          // ignore: cast_nullable_to_non_nullable
          : requireModalities as int?,
    );
  }
}

extension $IntelCorroborationEventQualityCopyWith
    on IntelCorroborationEventQuality {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelCorroborationEventQuality.copyWith(...)` or `instanceOfIntelCorroborationEventQuality.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelCorroborationEventQualityCWProxy get copyWith =>
      _$IntelCorroborationEventQualityCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelCorroborationEventQuality _$IntelCorroborationEventQualityFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelCorroborationEventQuality', json, ($checkedConvert) {
  final val = IntelCorroborationEventQuality(
    strongFamilies: $checkedConvert(
      'strongFamilies',
      (v) => (v as num?)?.toInt(),
    ),
    strongGroups: $checkedConvert('strongGroups', (v) => (v as num?)?.toInt()),
    moderateFamilies: $checkedConvert(
      'moderateFamilies',
      (v) => (v as num?)?.toInt(),
    ),
    requireModalities: $checkedConvert(
      'requireModalities',
      (v) => (v as num?)?.toInt(),
    ),
  );
  return val;
});

Map<String, dynamic> _$IntelCorroborationEventQualityToJson(
  IntelCorroborationEventQuality instance,
) => <String, dynamic>{
  'strongFamilies': ?instance.strongFamilies,
  'strongGroups': ?instance.strongGroups,
  'moderateFamilies': ?instance.moderateFamilies,
  'requireModalities': ?instance.requireModalities,
};
