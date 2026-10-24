// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_corroboration_situation_activation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelCorroborationSituationActivationCWProxy {
  IntelCorroborationSituationActivation minFamilies(int? minFamilies);

  IntelCorroborationSituationActivation minGroups(int? minGroups);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelCorroborationSituationActivation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelCorroborationSituationActivation(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelCorroborationSituationActivation call({
    int? minFamilies,
    int? minGroups,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelCorroborationSituationActivation.copyWith(...)` or call `instanceOfIntelCorroborationSituationActivation.copyWith.fieldName(value)` for a single field.
class _$IntelCorroborationSituationActivationCWProxyImpl
    implements _$IntelCorroborationSituationActivationCWProxy {
  const _$IntelCorroborationSituationActivationCWProxyImpl(this._value);

  final IntelCorroborationSituationActivation _value;

  @override
  IntelCorroborationSituationActivation minFamilies(int? minFamilies) =>
      call(minFamilies: minFamilies);

  @override
  IntelCorroborationSituationActivation minGroups(int? minGroups) =>
      call(minGroups: minGroups);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelCorroborationSituationActivation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelCorroborationSituationActivation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelCorroborationSituationActivation call({
    Object? minFamilies = const $CopyWithPlaceholder(),
    Object? minGroups = const $CopyWithPlaceholder(),
  }) {
    return IntelCorroborationSituationActivation(
      minFamilies: minFamilies == const $CopyWithPlaceholder()
          ? _value.minFamilies
          // ignore: cast_nullable_to_non_nullable
          : minFamilies as int?,
      minGroups: minGroups == const $CopyWithPlaceholder()
          ? _value.minGroups
          // ignore: cast_nullable_to_non_nullable
          : minGroups as int?,
    );
  }
}

extension $IntelCorroborationSituationActivationCopyWith
    on IntelCorroborationSituationActivation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelCorroborationSituationActivation.copyWith(...)` or `instanceOfIntelCorroborationSituationActivation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelCorroborationSituationActivationCWProxy get copyWith =>
      _$IntelCorroborationSituationActivationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelCorroborationSituationActivation
_$IntelCorroborationSituationActivationFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IntelCorroborationSituationActivation', json, (
      $checkedConvert,
    ) {
      final val = IntelCorroborationSituationActivation(
        minFamilies: $checkedConvert(
          'minFamilies',
          (v) => (v as num?)?.toInt(),
        ),
        minGroups: $checkedConvert('minGroups', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$IntelCorroborationSituationActivationToJson(
  IntelCorroborationSituationActivation instance,
) => <String, dynamic>{
  'minFamilies': ?instance.minFamilies,
  'minGroups': ?instance.minGroups,
};
