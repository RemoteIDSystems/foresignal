// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisPatchCWProxy {
  HypothesisPatch label(String? label);

  HypothesisPatch statement(String? statement);

  HypothesisPatch priorBasis(HypothesisInputPriorBasis? priorBasis);

  HypothesisPatch ledgerOutcome(String? ledgerOutcome);

  HypothesisPatch alternatives(List<String>? alternatives);

  HypothesisPatch probabilityOverride(
    HypothesisPatchProbabilityOverride? probabilityOverride,
  );

  HypothesisPatch version(int? version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisPatch call({
    String? label,
    String? statement,
    HypothesisInputPriorBasis? priorBasis,
    String? ledgerOutcome,
    List<String>? alternatives,
    HypothesisPatchProbabilityOverride? probabilityOverride,
    int? version,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisPatch.copyWith(...)` or call `instanceOfHypothesisPatch.copyWith.fieldName(value)` for a single field.
class _$HypothesisPatchCWProxyImpl implements _$HypothesisPatchCWProxy {
  const _$HypothesisPatchCWProxyImpl(this._value);

  final HypothesisPatch _value;

  @override
  HypothesisPatch label(String? label) => call(label: label);

  @override
  HypothesisPatch statement(String? statement) => call(statement: statement);

  @override
  HypothesisPatch priorBasis(HypothesisInputPriorBasis? priorBasis) =>
      call(priorBasis: priorBasis);

  @override
  HypothesisPatch ledgerOutcome(String? ledgerOutcome) =>
      call(ledgerOutcome: ledgerOutcome);

  @override
  HypothesisPatch alternatives(List<String>? alternatives) =>
      call(alternatives: alternatives);

  @override
  HypothesisPatch probabilityOverride(
    HypothesisPatchProbabilityOverride? probabilityOverride,
  ) => call(probabilityOverride: probabilityOverride);

  @override
  HypothesisPatch version(int? version) => call(version: version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisPatch call({
    Object? label = const $CopyWithPlaceholder(),
    Object? statement = const $CopyWithPlaceholder(),
    Object? priorBasis = const $CopyWithPlaceholder(),
    Object? ledgerOutcome = const $CopyWithPlaceholder(),
    Object? alternatives = const $CopyWithPlaceholder(),
    Object? probabilityOverride = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
  }) {
    return HypothesisPatch(
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
      statement: statement == const $CopyWithPlaceholder()
          ? _value.statement
          // ignore: cast_nullable_to_non_nullable
          : statement as String?,
      priorBasis: priorBasis == const $CopyWithPlaceholder()
          ? _value.priorBasis
          // ignore: cast_nullable_to_non_nullable
          : priorBasis as HypothesisInputPriorBasis?,
      ledgerOutcome: ledgerOutcome == const $CopyWithPlaceholder()
          ? _value.ledgerOutcome
          // ignore: cast_nullable_to_non_nullable
          : ledgerOutcome as String?,
      alternatives: alternatives == const $CopyWithPlaceholder()
          ? _value.alternatives
          // ignore: cast_nullable_to_non_nullable
          : alternatives as List<String>?,
      probabilityOverride: probabilityOverride == const $CopyWithPlaceholder()
          ? _value.probabilityOverride
          // ignore: cast_nullable_to_non_nullable
          : probabilityOverride as HypothesisPatchProbabilityOverride?,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int?,
    );
  }
}

extension $HypothesisPatchCopyWith on HypothesisPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisPatch.copyWith(...)` or `instanceOfHypothesisPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisPatchCWProxy get copyWith => _$HypothesisPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisPatch _$HypothesisPatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('HypothesisPatch', json, ($checkedConvert) {
      final val = HypothesisPatch(
        label: $checkedConvert('label', (v) => v as String?),
        statement: $checkedConvert('statement', (v) => v as String?),
        priorBasis: $checkedConvert(
          'priorBasis',
          (v) => v == null
              ? null
              : HypothesisInputPriorBasis.fromJson(v as Map<String, dynamic>),
        ),
        ledgerOutcome: $checkedConvert('ledgerOutcome', (v) => v as String?),
        alternatives: $checkedConvert(
          'alternatives',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        probabilityOverride: $checkedConvert(
          'probabilityOverride',
          (v) => v == null
              ? null
              : HypothesisPatchProbabilityOverride.fromJson(
                  v as Map<String, dynamic>,
                ),
        ),
        version: $checkedConvert('version', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$HypothesisPatchToJson(HypothesisPatch instance) =>
    <String, dynamic>{
      'label': ?instance.label,
      'statement': ?instance.statement,
      'priorBasis': ?instance.priorBasis?.toJson(),
      'ledgerOutcome': ?instance.ledgerOutcome,
      'alternatives': ?instance.alternatives,
      'probabilityOverride': ?instance.probabilityOverride?.toJson(),
      'version': ?instance.version,
    };
