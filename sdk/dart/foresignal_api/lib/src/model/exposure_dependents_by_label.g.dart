// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exposure_dependents_by_label.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExposureDependentsByLabelCWProxy {
  ExposureDependentsByLabel confirmed(int? confirmed);

  ExposureDependentsByLabel likely(int? likely);

  ExposureDependentsByLabel possible(int? possible);

  ExposureDependentsByLabel speculative(int? speculative);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposureDependentsByLabel(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposureDependentsByLabel(...).copyWith(id: 12, name: "My name")
  /// ```
  ExposureDependentsByLabel call({
    int? confirmed,
    int? likely,
    int? possible,
    int? speculative,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExposureDependentsByLabel.copyWith(...)` or call `instanceOfExposureDependentsByLabel.copyWith.fieldName(value)` for a single field.
class _$ExposureDependentsByLabelCWProxyImpl
    implements _$ExposureDependentsByLabelCWProxy {
  const _$ExposureDependentsByLabelCWProxyImpl(this._value);

  final ExposureDependentsByLabel _value;

  @override
  ExposureDependentsByLabel confirmed(int? confirmed) =>
      call(confirmed: confirmed);

  @override
  ExposureDependentsByLabel likely(int? likely) => call(likely: likely);

  @override
  ExposureDependentsByLabel possible(int? possible) => call(possible: possible);

  @override
  ExposureDependentsByLabel speculative(int? speculative) =>
      call(speculative: speculative);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposureDependentsByLabel(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposureDependentsByLabel(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExposureDependentsByLabel call({
    Object? confirmed = const $CopyWithPlaceholder(),
    Object? likely = const $CopyWithPlaceholder(),
    Object? possible = const $CopyWithPlaceholder(),
    Object? speculative = const $CopyWithPlaceholder(),
  }) {
    return ExposureDependentsByLabel(
      confirmed: confirmed == const $CopyWithPlaceholder()
          ? _value.confirmed
          // ignore: cast_nullable_to_non_nullable
          : confirmed as int?,
      likely: likely == const $CopyWithPlaceholder()
          ? _value.likely
          // ignore: cast_nullable_to_non_nullable
          : likely as int?,
      possible: possible == const $CopyWithPlaceholder()
          ? _value.possible
          // ignore: cast_nullable_to_non_nullable
          : possible as int?,
      speculative: speculative == const $CopyWithPlaceholder()
          ? _value.speculative
          // ignore: cast_nullable_to_non_nullable
          : speculative as int?,
    );
  }
}

extension $ExposureDependentsByLabelCopyWith on ExposureDependentsByLabel {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExposureDependentsByLabel.copyWith(...)` or `instanceOfExposureDependentsByLabel.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExposureDependentsByLabelCWProxy get copyWith =>
      _$ExposureDependentsByLabelCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExposureDependentsByLabel _$ExposureDependentsByLabelFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExposureDependentsByLabel', json, ($checkedConvert) {
  final val = ExposureDependentsByLabel(
    confirmed: $checkedConvert('confirmed', (v) => (v as num?)?.toInt()),
    likely: $checkedConvert('likely', (v) => (v as num?)?.toInt()),
    possible: $checkedConvert('possible', (v) => (v as num?)?.toInt()),
    speculative: $checkedConvert('speculative', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$ExposureDependentsByLabelToJson(
  ExposureDependentsByLabel instance,
) => <String, dynamic>{
  'confirmed': ?instance.confirmed,
  'likely': ?instance.likely,
  'possible': ?instance.possible,
  'speculative': ?instance.speculative,
};
