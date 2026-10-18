// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tuning_suggestion_expected_effect.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TuningSuggestionExpectedEffectCWProxy {
  TuningSuggestionExpectedEffect wouldFire(int? wouldFire);

  TuningSuggestionExpectedEffect removedNegatives(int? removedNegatives);

  TuningSuggestionExpectedEffect keptPositives(int? keptPositives);

  TuningSuggestionExpectedEffect backtestId(String? backtestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TuningSuggestionExpectedEffect(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TuningSuggestionExpectedEffect(...).copyWith(id: 12, name: "My name")
  /// ```
  TuningSuggestionExpectedEffect call({
    int? wouldFire,
    int? removedNegatives,
    int? keptPositives,
    String? backtestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTuningSuggestionExpectedEffect.copyWith(...)` or call `instanceOfTuningSuggestionExpectedEffect.copyWith.fieldName(value)` for a single field.
class _$TuningSuggestionExpectedEffectCWProxyImpl
    implements _$TuningSuggestionExpectedEffectCWProxy {
  const _$TuningSuggestionExpectedEffectCWProxyImpl(this._value);

  final TuningSuggestionExpectedEffect _value;

  @override
  TuningSuggestionExpectedEffect wouldFire(int? wouldFire) =>
      call(wouldFire: wouldFire);

  @override
  TuningSuggestionExpectedEffect removedNegatives(int? removedNegatives) =>
      call(removedNegatives: removedNegatives);

  @override
  TuningSuggestionExpectedEffect keptPositives(int? keptPositives) =>
      call(keptPositives: keptPositives);

  @override
  TuningSuggestionExpectedEffect backtestId(String? backtestId) =>
      call(backtestId: backtestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TuningSuggestionExpectedEffect(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TuningSuggestionExpectedEffect(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TuningSuggestionExpectedEffect call({
    Object? wouldFire = const $CopyWithPlaceholder(),
    Object? removedNegatives = const $CopyWithPlaceholder(),
    Object? keptPositives = const $CopyWithPlaceholder(),
    Object? backtestId = const $CopyWithPlaceholder(),
  }) {
    return TuningSuggestionExpectedEffect(
      wouldFire: wouldFire == const $CopyWithPlaceholder()
          ? _value.wouldFire
          // ignore: cast_nullable_to_non_nullable
          : wouldFire as int?,
      removedNegatives: removedNegatives == const $CopyWithPlaceholder()
          ? _value.removedNegatives
          // ignore: cast_nullable_to_non_nullable
          : removedNegatives as int?,
      keptPositives: keptPositives == const $CopyWithPlaceholder()
          ? _value.keptPositives
          // ignore: cast_nullable_to_non_nullable
          : keptPositives as int?,
      backtestId: backtestId == const $CopyWithPlaceholder()
          ? _value.backtestId
          // ignore: cast_nullable_to_non_nullable
          : backtestId as String?,
    );
  }
}

extension $TuningSuggestionExpectedEffectCopyWith
    on TuningSuggestionExpectedEffect {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTuningSuggestionExpectedEffect.copyWith(...)` or `instanceOfTuningSuggestionExpectedEffect.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TuningSuggestionExpectedEffectCWProxy get copyWith =>
      _$TuningSuggestionExpectedEffectCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TuningSuggestionExpectedEffect _$TuningSuggestionExpectedEffectFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TuningSuggestionExpectedEffect', json, ($checkedConvert) {
  final val = TuningSuggestionExpectedEffect(
    wouldFire: $checkedConvert('wouldFire', (v) => (v as num?)?.toInt()),
    removedNegatives: $checkedConvert(
      'removedNegatives',
      (v) => (v as num?)?.toInt(),
    ),
    keptPositives: $checkedConvert(
      'keptPositives',
      (v) => (v as num?)?.toInt(),
    ),
    backtestId: $checkedConvert('backtestId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$TuningSuggestionExpectedEffectToJson(
  TuningSuggestionExpectedEffect instance,
) => <String, dynamic>{
  'wouldFire': ?instance.wouldFire,
  'removedNegatives': ?instance.removedNegatives,
  'keptPositives': ?instance.keptPositives,
  'backtestId': ?instance.backtestId,
};
