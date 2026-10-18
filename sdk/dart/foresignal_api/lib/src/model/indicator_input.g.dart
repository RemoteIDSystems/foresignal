// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'indicator_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IndicatorInputCWProxy {
  IndicatorInput description(String description);

  IndicatorInput signal(IndicatorSignal signal);

  IndicatorInput signalFamily(String signalFamily);

  IndicatorInput credibility(IndicatorInputCredibilityEnum credibility);

  IndicatorInput relevance(IndicatorInputRelevanceEnum relevance);

  IndicatorInput expectedBy(DateTime? expectedBy);

  IndicatorInput lr(Map<String, IndicatorLikelihood> lr);

  IndicatorInput absenceLr(Map<String, num>? absenceLr);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorInput(...).copyWith(id: 12, name: "My name")
  /// ```
  IndicatorInput call({
    String description,
    IndicatorSignal signal,
    String signalFamily,
    IndicatorInputCredibilityEnum credibility,
    IndicatorInputRelevanceEnum relevance,
    DateTime? expectedBy,
    Map<String, IndicatorLikelihood> lr,
    Map<String, num>? absenceLr,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIndicatorInput.copyWith(...)` or call `instanceOfIndicatorInput.copyWith.fieldName(value)` for a single field.
class _$IndicatorInputCWProxyImpl implements _$IndicatorInputCWProxy {
  const _$IndicatorInputCWProxyImpl(this._value);

  final IndicatorInput _value;

  @override
  IndicatorInput description(String description) =>
      call(description: description);

  @override
  IndicatorInput signal(IndicatorSignal signal) => call(signal: signal);

  @override
  IndicatorInput signalFamily(String signalFamily) =>
      call(signalFamily: signalFamily);

  @override
  IndicatorInput credibility(IndicatorInputCredibilityEnum credibility) =>
      call(credibility: credibility);

  @override
  IndicatorInput relevance(IndicatorInputRelevanceEnum relevance) =>
      call(relevance: relevance);

  @override
  IndicatorInput expectedBy(DateTime? expectedBy) =>
      call(expectedBy: expectedBy);

  @override
  IndicatorInput lr(Map<String, IndicatorLikelihood> lr) => call(lr: lr);

  @override
  IndicatorInput absenceLr(Map<String, num>? absenceLr) =>
      call(absenceLr: absenceLr);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IndicatorInput call({
    Object? description = const $CopyWithPlaceholder(),
    Object? signal = const $CopyWithPlaceholder(),
    Object? signalFamily = const $CopyWithPlaceholder(),
    Object? credibility = const $CopyWithPlaceholder(),
    Object? relevance = const $CopyWithPlaceholder(),
    Object? expectedBy = const $CopyWithPlaceholder(),
    Object? lr = const $CopyWithPlaceholder(),
    Object? absenceLr = const $CopyWithPlaceholder(),
  }) {
    return IndicatorInput(
      description:
          description == const $CopyWithPlaceholder() || description == null
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String,
      signal: signal == const $CopyWithPlaceholder() || signal == null
          ? _value.signal
          // ignore: cast_nullable_to_non_nullable
          : signal as IndicatorSignal,
      signalFamily:
          signalFamily == const $CopyWithPlaceholder() || signalFamily == null
          ? _value.signalFamily
          // ignore: cast_nullable_to_non_nullable
          : signalFamily as String,
      credibility:
          credibility == const $CopyWithPlaceholder() || credibility == null
          ? _value.credibility
          // ignore: cast_nullable_to_non_nullable
          : credibility as IndicatorInputCredibilityEnum,
      relevance: relevance == const $CopyWithPlaceholder() || relevance == null
          ? _value.relevance
          // ignore: cast_nullable_to_non_nullable
          : relevance as IndicatorInputRelevanceEnum,
      expectedBy: expectedBy == const $CopyWithPlaceholder()
          ? _value.expectedBy
          // ignore: cast_nullable_to_non_nullable
          : expectedBy as DateTime?,
      lr: lr == const $CopyWithPlaceholder() || lr == null
          ? _value.lr
          // ignore: cast_nullable_to_non_nullable
          : lr as Map<String, IndicatorLikelihood>,
      absenceLr: absenceLr == const $CopyWithPlaceholder()
          ? _value.absenceLr
          // ignore: cast_nullable_to_non_nullable
          : absenceLr as Map<String, num>?,
    );
  }
}

extension $IndicatorInputCopyWith on IndicatorInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIndicatorInput.copyWith(...)` or `instanceOfIndicatorInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IndicatorInputCWProxy get copyWith => _$IndicatorInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IndicatorInput _$IndicatorInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IndicatorInput', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'description',
          'signal',
          'signalFamily',
          'credibility',
          'relevance',
          'lr',
        ],
      );
      final val = IndicatorInput(
        description: $checkedConvert('description', (v) => v as String),
        signal: $checkedConvert(
          'signal',
          (v) => IndicatorSignal.fromJson(v as Map<String, dynamic>),
        ),
        signalFamily: $checkedConvert('signalFamily', (v) => v as String),
        credibility: $checkedConvert(
          'credibility',
          (v) => $enumDecode(_$IndicatorInputCredibilityEnumEnumMap, v),
        ),
        relevance: $checkedConvert(
          'relevance',
          (v) => $enumDecode(_$IndicatorInputRelevanceEnumEnumMap, v),
        ),
        expectedBy: $checkedConvert(
          'expectedBy',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        lr: $checkedConvert(
          'lr',
          (v) => (v as Map<String, dynamic>).map(
            (k, e) => MapEntry(
              k,
              IndicatorLikelihood.fromJson(e as Map<String, dynamic>),
            ),
          ),
        ),
        absenceLr: $checkedConvert(
          'absenceLr',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as num),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$IndicatorInputToJson(
  IndicatorInput instance,
) => <String, dynamic>{
  'description': instance.description,
  'signal': instance.signal.toJson(),
  'signalFamily': instance.signalFamily,
  'credibility': _$IndicatorInputCredibilityEnumEnumMap[instance.credibility]!,
  'relevance': _$IndicatorInputRelevanceEnumEnumMap[instance.relevance]!,
  'expectedBy': ?instance.expectedBy?.toIso8601String(),
  'lr': instance.lr.map((k, e) => MapEntry(k, e.toJson())),
  'absenceLr': ?instance.absenceLr,
};

const _$IndicatorInputCredibilityEnumEnumMap = {
  IndicatorInputCredibilityEnum.H: 'H',
  IndicatorInputCredibilityEnum.M: 'M',
  IndicatorInputCredibilityEnum.L: 'L',
};

const _$IndicatorInputRelevanceEnumEnumMap = {
  IndicatorInputRelevanceEnum.H: 'H',
  IndicatorInputRelevanceEnum.M: 'M',
  IndicatorInputRelevanceEnum.L: 'L',
};
