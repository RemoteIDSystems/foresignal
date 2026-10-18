// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sample_track_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SampleTrackRequestCWProxy {
  SampleTrackRequest record(String? record);

  SampleTrackRequest from(DateTime? from);

  SampleTrackRequest to(DateTime? to);

  SampleTrackRequest points(List<SampleTrackRequestPointsInner>? points);

  SampleTrackRequest products(List<String> products);

  SampleTrackRequest maxGap(String? maxGap);

  SampleTrackRequest run(String? run);

  SampleTrackRequest units(SampleTrackRequestUnitsEnum? units);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleTrackRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleTrackRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  SampleTrackRequest call({
    String? record,
    DateTime? from,
    DateTime? to,
    List<SampleTrackRequestPointsInner>? points,
    List<String> products,
    String? maxGap,
    String? run,
    SampleTrackRequestUnitsEnum? units,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSampleTrackRequest.copyWith(...)` or call `instanceOfSampleTrackRequest.copyWith.fieldName(value)` for a single field.
class _$SampleTrackRequestCWProxyImpl implements _$SampleTrackRequestCWProxy {
  const _$SampleTrackRequestCWProxyImpl(this._value);

  final SampleTrackRequest _value;

  @override
  SampleTrackRequest record(String? record) => call(record: record);

  @override
  SampleTrackRequest from(DateTime? from) => call(from: from);

  @override
  SampleTrackRequest to(DateTime? to) => call(to: to);

  @override
  SampleTrackRequest points(List<SampleTrackRequestPointsInner>? points) =>
      call(points: points);

  @override
  SampleTrackRequest products(List<String> products) =>
      call(products: products);

  @override
  SampleTrackRequest maxGap(String? maxGap) => call(maxGap: maxGap);

  @override
  SampleTrackRequest run(String? run) => call(run: run);

  @override
  SampleTrackRequest units(SampleTrackRequestUnitsEnum? units) =>
      call(units: units);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleTrackRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleTrackRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SampleTrackRequest call({
    Object? record = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? points = const $CopyWithPlaceholder(),
    Object? products = const $CopyWithPlaceholder(),
    Object? maxGap = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? units = const $CopyWithPlaceholder(),
  }) {
    return SampleTrackRequest(
      record: record == const $CopyWithPlaceholder()
          ? _value.record
          // ignore: cast_nullable_to_non_nullable
          : record as String?,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      points: points == const $CopyWithPlaceholder()
          ? _value.points
          // ignore: cast_nullable_to_non_nullable
          : points as List<SampleTrackRequestPointsInner>?,
      products: products == const $CopyWithPlaceholder() || products == null
          ? _value.products
          // ignore: cast_nullable_to_non_nullable
          : products as List<String>,
      maxGap: maxGap == const $CopyWithPlaceholder()
          ? _value.maxGap
          // ignore: cast_nullable_to_non_nullable
          : maxGap as String?,
      run: run == const $CopyWithPlaceholder()
          ? _value.run
          // ignore: cast_nullable_to_non_nullable
          : run as String?,
      units: units == const $CopyWithPlaceholder()
          ? _value.units
          // ignore: cast_nullable_to_non_nullable
          : units as SampleTrackRequestUnitsEnum?,
    );
  }
}

extension $SampleTrackRequestCopyWith on SampleTrackRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSampleTrackRequest.copyWith(...)` or `instanceOfSampleTrackRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SampleTrackRequestCWProxy get copyWith =>
      _$SampleTrackRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SampleTrackRequest _$SampleTrackRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SampleTrackRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['products']);
      final val = SampleTrackRequest(
        record: $checkedConvert('record', (v) => v as String?),
        from: $checkedConvert(
          'from',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        to: $checkedConvert(
          'to',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        points: $checkedConvert(
          'points',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => SampleTrackRequestPointsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        products: $checkedConvert(
          'products',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        maxGap: $checkedConvert('maxGap', (v) => v as String?),
        run: $checkedConvert('run', (v) => v as String?),
        units: $checkedConvert(
          'units',
          (v) => $enumDecodeNullable(_$SampleTrackRequestUnitsEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SampleTrackRequestToJson(SampleTrackRequest instance) =>
    <String, dynamic>{
      'record': ?instance.record,
      'from': ?instance.from?.toIso8601String(),
      'to': ?instance.to?.toIso8601String(),
      'points': ?instance.points?.map((e) => e.toJson()).toList(),
      'products': instance.products,
      'maxGap': ?instance.maxGap,
      'run': ?instance.run,
      'units': ?_$SampleTrackRequestUnitsEnumEnumMap[instance.units],
    };

const _$SampleTrackRequestUnitsEnumEnumMap = {
  SampleTrackRequestUnitsEnum.metric: 'metric',
  SampleTrackRequestUnitsEnum.imperial: 'imperial',
};
