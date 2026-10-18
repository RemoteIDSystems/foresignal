// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'detectability.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DetectabilityCWProxy {
  Detectability family(String family);

  Detectability regionCode(String? regionCode);

  Detectability cell(String? cell);

  Detectability band(String? band);

  Detectability at(DateTime at);

  Detectability p(num p);

  Detectability lo(num lo);

  Detectability hi(num hi);

  Detectability methods(List<DetectabilityMethodsEnum> methods);

  Detectability sources(List<String> sources);

  Detectability confidence(ConfidenceLevel? confidence);

  Detectability absenceOk(bool absenceOk);

  Detectability reason(String? reason);

  Detectability estimates(List<ObservabilityEstimate>? estimates);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Detectability(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Detectability(...).copyWith(id: 12, name: "My name")
  /// ```
  Detectability call({
    String family,
    String? regionCode,
    String? cell,
    String? band,
    DateTime at,
    num p,
    num lo,
    num hi,
    List<DetectabilityMethodsEnum> methods,
    List<String> sources,
    ConfidenceLevel? confidence,
    bool absenceOk,
    String? reason,
    List<ObservabilityEstimate>? estimates,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDetectability.copyWith(...)` or call `instanceOfDetectability.copyWith.fieldName(value)` for a single field.
class _$DetectabilityCWProxyImpl implements _$DetectabilityCWProxy {
  const _$DetectabilityCWProxyImpl(this._value);

  final Detectability _value;

  @override
  Detectability family(String family) => call(family: family);

  @override
  Detectability regionCode(String? regionCode) => call(regionCode: regionCode);

  @override
  Detectability cell(String? cell) => call(cell: cell);

  @override
  Detectability band(String? band) => call(band: band);

  @override
  Detectability at(DateTime at) => call(at: at);

  @override
  Detectability p(num p) => call(p: p);

  @override
  Detectability lo(num lo) => call(lo: lo);

  @override
  Detectability hi(num hi) => call(hi: hi);

  @override
  Detectability methods(List<DetectabilityMethodsEnum> methods) =>
      call(methods: methods);

  @override
  Detectability sources(List<String> sources) => call(sources: sources);

  @override
  Detectability confidence(ConfidenceLevel? confidence) =>
      call(confidence: confidence);

  @override
  Detectability absenceOk(bool absenceOk) => call(absenceOk: absenceOk);

  @override
  Detectability reason(String? reason) => call(reason: reason);

  @override
  Detectability estimates(List<ObservabilityEstimate>? estimates) =>
      call(estimates: estimates);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Detectability(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Detectability(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Detectability call({
    Object? family = const $CopyWithPlaceholder(),
    Object? regionCode = const $CopyWithPlaceholder(),
    Object? cell = const $CopyWithPlaceholder(),
    Object? band = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? p = const $CopyWithPlaceholder(),
    Object? lo = const $CopyWithPlaceholder(),
    Object? hi = const $CopyWithPlaceholder(),
    Object? methods = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? absenceOk = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? estimates = const $CopyWithPlaceholder(),
  }) {
    return Detectability(
      family: family == const $CopyWithPlaceholder() || family == null
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String,
      regionCode: regionCode == const $CopyWithPlaceholder()
          ? _value.regionCode
          // ignore: cast_nullable_to_non_nullable
          : regionCode as String?,
      cell: cell == const $CopyWithPlaceholder()
          ? _value.cell
          // ignore: cast_nullable_to_non_nullable
          : cell as String?,
      band: band == const $CopyWithPlaceholder()
          ? _value.band
          // ignore: cast_nullable_to_non_nullable
          : band as String?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      p: p == const $CopyWithPlaceholder() || p == null
          ? _value.p
          // ignore: cast_nullable_to_non_nullable
          : p as num,
      lo: lo == const $CopyWithPlaceholder() || lo == null
          ? _value.lo
          // ignore: cast_nullable_to_non_nullable
          : lo as num,
      hi: hi == const $CopyWithPlaceholder() || hi == null
          ? _value.hi
          // ignore: cast_nullable_to_non_nullable
          : hi as num,
      methods: methods == const $CopyWithPlaceholder() || methods == null
          ? _value.methods
          // ignore: cast_nullable_to_non_nullable
          : methods as List<DetectabilityMethodsEnum>,
      sources: sources == const $CopyWithPlaceholder() || sources == null
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>,
      confidence: confidence == const $CopyWithPlaceholder()
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as ConfidenceLevel?,
      absenceOk: absenceOk == const $CopyWithPlaceholder() || absenceOk == null
          ? _value.absenceOk
          // ignore: cast_nullable_to_non_nullable
          : absenceOk as bool,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
      estimates: estimates == const $CopyWithPlaceholder()
          ? _value.estimates
          // ignore: cast_nullable_to_non_nullable
          : estimates as List<ObservabilityEstimate>?,
    );
  }
}

extension $DetectabilityCopyWith on Detectability {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDetectability.copyWith(...)` or `instanceOfDetectability.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DetectabilityCWProxy get copyWith => _$DetectabilityCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Detectability _$DetectabilityFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Detectability', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'family',
          'at',
          'p',
          'lo',
          'hi',
          'methods',
          'sources',
          'absenceOk',
        ],
      );
      final val = Detectability(
        family: $checkedConvert('family', (v) => v as String),
        regionCode: $checkedConvert('regionCode', (v) => v as String?),
        cell: $checkedConvert('cell', (v) => v as String?),
        band: $checkedConvert('band', (v) => v as String?),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
        p: $checkedConvert('p', (v) => v as num),
        lo: $checkedConvert('lo', (v) => v as num),
        hi: $checkedConvert('hi', (v) => v as num),
        methods: $checkedConvert(
          'methods',
          (v) => (v as List<dynamic>)
              .map((e) => $enumDecode(_$DetectabilityMethodsEnumEnumMap, e))
              .toList(),
        ),
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        confidence: $checkedConvert(
          'confidence',
          (v) => $enumDecodeNullable(_$ConfidenceLevelEnumMap, v),
        ),
        absenceOk: $checkedConvert('absenceOk', (v) => v as bool),
        reason: $checkedConvert('reason', (v) => v as String?),
        estimates: $checkedConvert(
          'estimates',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) =>
                    ObservabilityEstimate.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$DetectabilityToJson(Detectability instance) =>
    <String, dynamic>{
      'family': instance.family,
      'regionCode': ?instance.regionCode,
      'cell': ?instance.cell,
      'band': ?instance.band,
      'at': instance.at.toIso8601String(),
      'p': instance.p,
      'lo': instance.lo,
      'hi': instance.hi,
      'methods': instance.methods
          .map((e) => _$DetectabilityMethodsEnumEnumMap[e]!)
          .toList(),
      'sources': instance.sources,
      'confidence': ?_$ConfidenceLevelEnumMap[instance.confidence],
      'absenceOk': instance.absenceOk,
      'reason': ?instance.reason,
      'estimates': ?instance.estimates?.map((e) => e.toJson()).toList(),
    };

const _$DetectabilityMethodsEnumEnumMap = {
  DetectabilityMethodsEnum.captureRecapture: 'capture-recapture',
  DetectabilityMethodsEnum.presenceProxy: 'presence-proxy',
  DetectabilityMethodsEnum.receiverRange: 'receiver-range',
  DetectabilityMethodsEnum.declared: 'declared',
  DetectabilityMethodsEnum.platformVolume: 'platform-volume',
};

const _$ConfidenceLevelEnumMap = {
  ConfidenceLevel.low: 'low',
  ConfidenceLevel.moderate: 'moderate',
  ConfidenceLevel.high: 'high',
};
