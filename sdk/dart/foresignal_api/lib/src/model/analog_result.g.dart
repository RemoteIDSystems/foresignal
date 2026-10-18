// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analog_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalogResultCWProxy {
  AnalogResult target(AnalogResultTarget target);

  AnalogResult k(int k);

  AnalogResult countFound(int countFound);

  AnalogResult method(AnalogResultMethodEnum method);

  AnalogResult methodNote(String? methodNote);

  AnalogResult signature(AnalogResultSignature? signature);

  AnalogResult analogs(List<Analog> analogs);

  AnalogResult outcomeDistribution(
    AnalogResultOutcomeDistribution? outcomeDistribution,
  );

  AnalogResult caveats(List<Caveat> caveats);

  AnalogResult detector(DetectorRef detector);

  AnalogResult manifestId(String manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalogResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalogResult(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalogResult call({
    AnalogResultTarget target,
    int k,
    int countFound,
    AnalogResultMethodEnum method,
    String? methodNote,
    AnalogResultSignature? signature,
    List<Analog> analogs,
    AnalogResultOutcomeDistribution? outcomeDistribution,
    List<Caveat> caveats,
    DetectorRef detector,
    String manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalogResult.copyWith(...)` or call `instanceOfAnalogResult.copyWith.fieldName(value)` for a single field.
class _$AnalogResultCWProxyImpl implements _$AnalogResultCWProxy {
  const _$AnalogResultCWProxyImpl(this._value);

  final AnalogResult _value;

  @override
  AnalogResult target(AnalogResultTarget target) => call(target: target);

  @override
  AnalogResult k(int k) => call(k: k);

  @override
  AnalogResult countFound(int countFound) => call(countFound: countFound);

  @override
  AnalogResult method(AnalogResultMethodEnum method) => call(method: method);

  @override
  AnalogResult methodNote(String? methodNote) => call(methodNote: methodNote);

  @override
  AnalogResult signature(AnalogResultSignature? signature) =>
      call(signature: signature);

  @override
  AnalogResult analogs(List<Analog> analogs) => call(analogs: analogs);

  @override
  AnalogResult outcomeDistribution(
    AnalogResultOutcomeDistribution? outcomeDistribution,
  ) => call(outcomeDistribution: outcomeDistribution);

  @override
  AnalogResult caveats(List<Caveat> caveats) => call(caveats: caveats);

  @override
  AnalogResult detector(DetectorRef detector) => call(detector: detector);

  @override
  AnalogResult manifestId(String manifestId) => call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalogResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalogResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalogResult call({
    Object? target = const $CopyWithPlaceholder(),
    Object? k = const $CopyWithPlaceholder(),
    Object? countFound = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? methodNote = const $CopyWithPlaceholder(),
    Object? signature = const $CopyWithPlaceholder(),
    Object? analogs = const $CopyWithPlaceholder(),
    Object? outcomeDistribution = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return AnalogResult(
      target: target == const $CopyWithPlaceholder() || target == null
          ? _value.target
          // ignore: cast_nullable_to_non_nullable
          : target as AnalogResultTarget,
      k: k == const $CopyWithPlaceholder() || k == null
          ? _value.k
          // ignore: cast_nullable_to_non_nullable
          : k as int,
      countFound:
          countFound == const $CopyWithPlaceholder() || countFound == null
          ? _value.countFound
          // ignore: cast_nullable_to_non_nullable
          : countFound as int,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as AnalogResultMethodEnum,
      methodNote: methodNote == const $CopyWithPlaceholder()
          ? _value.methodNote
          // ignore: cast_nullable_to_non_nullable
          : methodNote as String?,
      signature: signature == const $CopyWithPlaceholder()
          ? _value.signature
          // ignore: cast_nullable_to_non_nullable
          : signature as AnalogResultSignature?,
      analogs: analogs == const $CopyWithPlaceholder() || analogs == null
          ? _value.analogs
          // ignore: cast_nullable_to_non_nullable
          : analogs as List<Analog>,
      outcomeDistribution: outcomeDistribution == const $CopyWithPlaceholder()
          ? _value.outcomeDistribution
          // ignore: cast_nullable_to_non_nullable
          : outcomeDistribution as AnalogResultOutcomeDistribution?,
      caveats: caveats == const $CopyWithPlaceholder() || caveats == null
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>,
      detector: detector == const $CopyWithPlaceholder() || detector == null
          ? _value.detector
          // ignore: cast_nullable_to_non_nullable
          : detector as DetectorRef,
      manifestId:
          manifestId == const $CopyWithPlaceholder() || manifestId == null
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String,
    );
  }
}

extension $AnalogResultCopyWith on AnalogResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalogResult.copyWith(...)` or `instanceOfAnalogResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalogResultCWProxy get copyWith => _$AnalogResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalogResult _$AnalogResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalogResult', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'target',
          'k',
          'countFound',
          'method',
          'analogs',
          'caveats',
          'detector',
          'manifestId',
        ],
      );
      final val = AnalogResult(
        target: $checkedConvert(
          'target',
          (v) => AnalogResultTarget.fromJson(v as Map<String, dynamic>),
        ),
        k: $checkedConvert('k', (v) => (v as num).toInt()),
        countFound: $checkedConvert('countFound', (v) => (v as num).toInt()),
        method: $checkedConvert(
          'method',
          (v) => $enumDecode(_$AnalogResultMethodEnumEnumMap, v),
        ),
        methodNote: $checkedConvert('methodNote', (v) => v as String?),
        signature: $checkedConvert(
          'signature',
          (v) => v == null
              ? null
              : AnalogResultSignature.fromJson(v as Map<String, dynamic>),
        ),
        analogs: $checkedConvert(
          'analogs',
          (v) => (v as List<dynamic>)
              .map((e) => Analog.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        outcomeDistribution: $checkedConvert(
          'outcomeDistribution',
          (v) => v == null
              ? null
              : AnalogResultOutcomeDistribution.fromJson(
                  v as Map<String, dynamic>,
                ),
        ),
        caveats: $checkedConvert(
          'caveats',
          (v) => (v as List<dynamic>)
              .map((e) => Caveat.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        detector: $checkedConvert(
          'detector',
          (v) => DetectorRef.fromJson(v as Map<String, dynamic>),
        ),
        manifestId: $checkedConvert('manifestId', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$AnalogResultToJson(AnalogResult instance) =>
    <String, dynamic>{
      'target': instance.target.toJson(),
      'k': instance.k,
      'countFound': instance.countFound,
      'method': _$AnalogResultMethodEnumEnumMap[instance.method]!,
      'methodNote': ?instance.methodNote,
      'signature': ?instance.signature?.toJson(),
      'analogs': instance.analogs.map((e) => e.toJson()).toList(),
      'outcomeDistribution': ?instance.outcomeDistribution?.toJson(),
      'caveats': instance.caveats.map((e) => e.toJson()).toList(),
      'detector': instance.detector.toJson(),
      'manifestId': instance.manifestId,
    };

const _$AnalogResultMethodEnumEnumMap = {
  AnalogResultMethodEnum.fused: 'fused',
  AnalogResultMethodEnum.featureOnly: 'feature-only',
};
