// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_estimate.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleEstimateCWProxy {
  ChronicleEstimate chronicleId(String chronicleId);

  ChronicleEstimate computedAt(DateTime computedAt);

  ChronicleEstimate layers(List<ChronicleEstimateLayersInner> layers);

  ChronicleEstimate totalRowsPerDay(num totalRowsPerDay);

  ChronicleEstimate totalBytesPerDay(num totalBytesPerDay);

  ChronicleEstimate sizeCapBytes(int sizeCapBytes);

  ChronicleEstimate bytesUsed(int? bytesUsed);

  ChronicleEstimate daysToCap(num? daysToCap);

  ChronicleEstimate caveats(List<Caveat>? caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleEstimate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleEstimate(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleEstimate call({
    String chronicleId,
    DateTime computedAt,
    List<ChronicleEstimateLayersInner> layers,
    num totalRowsPerDay,
    num totalBytesPerDay,
    int sizeCapBytes,
    int? bytesUsed,
    num? daysToCap,
    List<Caveat>? caveats,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleEstimate.copyWith(...)` or call `instanceOfChronicleEstimate.copyWith.fieldName(value)` for a single field.
class _$ChronicleEstimateCWProxyImpl implements _$ChronicleEstimateCWProxy {
  const _$ChronicleEstimateCWProxyImpl(this._value);

  final ChronicleEstimate _value;

  @override
  ChronicleEstimate chronicleId(String chronicleId) =>
      call(chronicleId: chronicleId);

  @override
  ChronicleEstimate computedAt(DateTime computedAt) =>
      call(computedAt: computedAt);

  @override
  ChronicleEstimate layers(List<ChronicleEstimateLayersInner> layers) =>
      call(layers: layers);

  @override
  ChronicleEstimate totalRowsPerDay(num totalRowsPerDay) =>
      call(totalRowsPerDay: totalRowsPerDay);

  @override
  ChronicleEstimate totalBytesPerDay(num totalBytesPerDay) =>
      call(totalBytesPerDay: totalBytesPerDay);

  @override
  ChronicleEstimate sizeCapBytes(int sizeCapBytes) =>
      call(sizeCapBytes: sizeCapBytes);

  @override
  ChronicleEstimate bytesUsed(int? bytesUsed) => call(bytesUsed: bytesUsed);

  @override
  ChronicleEstimate daysToCap(num? daysToCap) => call(daysToCap: daysToCap);

  @override
  ChronicleEstimate caveats(List<Caveat>? caveats) => call(caveats: caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleEstimate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleEstimate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleEstimate call({
    Object? chronicleId = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
    Object? layers = const $CopyWithPlaceholder(),
    Object? totalRowsPerDay = const $CopyWithPlaceholder(),
    Object? totalBytesPerDay = const $CopyWithPlaceholder(),
    Object? sizeCapBytes = const $CopyWithPlaceholder(),
    Object? bytesUsed = const $CopyWithPlaceholder(),
    Object? daysToCap = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
  }) {
    return ChronicleEstimate(
      chronicleId:
          chronicleId == const $CopyWithPlaceholder() || chronicleId == null
          ? _value.chronicleId
          // ignore: cast_nullable_to_non_nullable
          : chronicleId as String,
      computedAt:
          computedAt == const $CopyWithPlaceholder() || computedAt == null
          ? _value.computedAt
          // ignore: cast_nullable_to_non_nullable
          : computedAt as DateTime,
      layers: layers == const $CopyWithPlaceholder() || layers == null
          ? _value.layers
          // ignore: cast_nullable_to_non_nullable
          : layers as List<ChronicleEstimateLayersInner>,
      totalRowsPerDay:
          totalRowsPerDay == const $CopyWithPlaceholder() ||
              totalRowsPerDay == null
          ? _value.totalRowsPerDay
          // ignore: cast_nullable_to_non_nullable
          : totalRowsPerDay as num,
      totalBytesPerDay:
          totalBytesPerDay == const $CopyWithPlaceholder() ||
              totalBytesPerDay == null
          ? _value.totalBytesPerDay
          // ignore: cast_nullable_to_non_nullable
          : totalBytesPerDay as num,
      sizeCapBytes:
          sizeCapBytes == const $CopyWithPlaceholder() || sizeCapBytes == null
          ? _value.sizeCapBytes
          // ignore: cast_nullable_to_non_nullable
          : sizeCapBytes as int,
      bytesUsed: bytesUsed == const $CopyWithPlaceholder()
          ? _value.bytesUsed
          // ignore: cast_nullable_to_non_nullable
          : bytesUsed as int?,
      daysToCap: daysToCap == const $CopyWithPlaceholder()
          ? _value.daysToCap
          // ignore: cast_nullable_to_non_nullable
          : daysToCap as num?,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
    );
  }
}

extension $ChronicleEstimateCopyWith on ChronicleEstimate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleEstimate.copyWith(...)` or `instanceOfChronicleEstimate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleEstimateCWProxy get copyWith =>
      _$ChronicleEstimateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleEstimate _$ChronicleEstimateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleEstimate', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'chronicleId',
          'computedAt',
          'layers',
          'totalRowsPerDay',
          'totalBytesPerDay',
          'sizeCapBytes',
        ],
      );
      final val = ChronicleEstimate(
        chronicleId: $checkedConvert('chronicleId', (v) => v as String),
        computedAt: $checkedConvert(
          'computedAt',
          (v) => DateTime.parse(v as String),
        ),
        layers: $checkedConvert(
          'layers',
          (v) => (v as List<dynamic>)
              .map(
                (e) => ChronicleEstimateLayersInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        totalRowsPerDay: $checkedConvert('totalRowsPerDay', (v) => v as num),
        totalBytesPerDay: $checkedConvert('totalBytesPerDay', (v) => v as num),
        sizeCapBytes: $checkedConvert(
          'sizeCapBytes',
          (v) => (v as num).toInt(),
        ),
        bytesUsed: $checkedConvert('bytesUsed', (v) => (v as num?)?.toInt()),
        daysToCap: $checkedConvert('daysToCap', (v) => v as num?),
        caveats: $checkedConvert(
          'caveats',
          (v) => (v as List<dynamic>?)
              ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleEstimateToJson(ChronicleEstimate instance) =>
    <String, dynamic>{
      'chronicleId': instance.chronicleId,
      'computedAt': instance.computedAt.toIso8601String(),
      'layers': instance.layers.map((e) => e.toJson()).toList(),
      'totalRowsPerDay': instance.totalRowsPerDay,
      'totalBytesPerDay': instance.totalBytesPerDay,
      'sizeCapBytes': instance.sizeCapBytes,
      'bytesUsed': ?instance.bytesUsed,
      'daysToCap': ?instance.daysToCap,
      'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
    };
