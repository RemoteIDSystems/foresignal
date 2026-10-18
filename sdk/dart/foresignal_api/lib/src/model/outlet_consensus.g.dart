// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'outlet_consensus.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OutletConsensusCWProxy {
  OutletConsensus outletId(String outletId);

  OutletConsensus dimension(Object? dimension);

  OutletConsensus median(num? median);

  OutletConsensus nProviders(int nProviders);

  OutletConsensus disagreement(num? disagreement);

  OutletConsensus bucket(OutletConsensusBucketEnum? bucket);

  OutletConsensus providers(List<OutletConsensusProvidersInner> providers);

  OutletConsensus computedAt(DateTime? computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OutletConsensus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OutletConsensus(...).copyWith(id: 12, name: "My name")
  /// ```
  OutletConsensus call({
    String outletId,
    Object? dimension,
    num? median,
    int nProviders,
    num? disagreement,
    OutletConsensusBucketEnum? bucket,
    List<OutletConsensusProvidersInner> providers,
    DateTime? computedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOutletConsensus.copyWith(...)` or call `instanceOfOutletConsensus.copyWith.fieldName(value)` for a single field.
class _$OutletConsensusCWProxyImpl implements _$OutletConsensusCWProxy {
  const _$OutletConsensusCWProxyImpl(this._value);

  final OutletConsensus _value;

  @override
  OutletConsensus outletId(String outletId) => call(outletId: outletId);

  @override
  OutletConsensus dimension(Object? dimension) => call(dimension: dimension);

  @override
  OutletConsensus median(num? median) => call(median: median);

  @override
  OutletConsensus nProviders(int nProviders) => call(nProviders: nProviders);

  @override
  OutletConsensus disagreement(num? disagreement) =>
      call(disagreement: disagreement);

  @override
  OutletConsensus bucket(OutletConsensusBucketEnum? bucket) =>
      call(bucket: bucket);

  @override
  OutletConsensus providers(List<OutletConsensusProvidersInner> providers) =>
      call(providers: providers);

  @override
  OutletConsensus computedAt(DateTime? computedAt) =>
      call(computedAt: computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OutletConsensus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OutletConsensus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OutletConsensus call({
    Object? outletId = const $CopyWithPlaceholder(),
    Object? dimension = const $CopyWithPlaceholder(),
    Object? median = const $CopyWithPlaceholder(),
    Object? nProviders = const $CopyWithPlaceholder(),
    Object? disagreement = const $CopyWithPlaceholder(),
    Object? bucket = const $CopyWithPlaceholder(),
    Object? providers = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
  }) {
    return OutletConsensus(
      outletId: outletId == const $CopyWithPlaceholder() || outletId == null
          ? _value.outletId
          // ignore: cast_nullable_to_non_nullable
          : outletId as String,
      dimension: dimension == const $CopyWithPlaceholder()
          ? _value.dimension
          // ignore: cast_nullable_to_non_nullable
          : dimension as Object?,
      median: median == const $CopyWithPlaceholder()
          ? _value.median
          // ignore: cast_nullable_to_non_nullable
          : median as num?,
      nProviders:
          nProviders == const $CopyWithPlaceholder() || nProviders == null
          ? _value.nProviders
          // ignore: cast_nullable_to_non_nullable
          : nProviders as int,
      disagreement: disagreement == const $CopyWithPlaceholder()
          ? _value.disagreement
          // ignore: cast_nullable_to_non_nullable
          : disagreement as num?,
      bucket: bucket == const $CopyWithPlaceholder()
          ? _value.bucket
          // ignore: cast_nullable_to_non_nullable
          : bucket as OutletConsensusBucketEnum?,
      providers: providers == const $CopyWithPlaceholder() || providers == null
          ? _value.providers
          // ignore: cast_nullable_to_non_nullable
          : providers as List<OutletConsensusProvidersInner>,
      computedAt: computedAt == const $CopyWithPlaceholder()
          ? _value.computedAt
          // ignore: cast_nullable_to_non_nullable
          : computedAt as DateTime?,
    );
  }
}

extension $OutletConsensusCopyWith on OutletConsensus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOutletConsensus.copyWith(...)` or `instanceOfOutletConsensus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OutletConsensusCWProxy get copyWith => _$OutletConsensusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OutletConsensus _$OutletConsensusFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OutletConsensus', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'outletId',
          'dimension',
          'nProviders',
          'providers',
        ],
      );
      final val = OutletConsensus(
        outletId: $checkedConvert('outletId', (v) => v as String),
        dimension: $checkedConvert('dimension', (v) => v),
        median: $checkedConvert('median', (v) => v as num?),
        nProviders: $checkedConvert('nProviders', (v) => (v as num).toInt()),
        disagreement: $checkedConvert('disagreement', (v) => v as num?),
        bucket: $checkedConvert(
          'bucket',
          (v) => $enumDecodeNullable(_$OutletConsensusBucketEnumEnumMap, v),
        ),
        providers: $checkedConvert(
          'providers',
          (v) => (v as List<dynamic>)
              .map(
                (e) => OutletConsensusProvidersInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        computedAt: $checkedConvert(
          'computedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OutletConsensusToJson(OutletConsensus instance) =>
    <String, dynamic>{
      'outletId': instance.outletId,
      'dimension': instance.dimension,
      'median': ?instance.median,
      'nProviders': instance.nProviders,
      'disagreement': ?instance.disagreement,
      'bucket': ?_$OutletConsensusBucketEnumEnumMap[instance.bucket],
      'providers': instance.providers.map((e) => e.toJson()).toList(),
      'computedAt': ?instance.computedAt?.toIso8601String(),
    };

const _$OutletConsensusBucketEnumEnumMap = {
  OutletConsensusBucketEnum.left: 'left',
  OutletConsensusBucketEnum.center: 'center',
  OutletConsensusBucketEnum.right: 'right',
};
