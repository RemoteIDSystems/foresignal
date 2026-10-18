// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_coverage_collection_extent_temporal.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcCoverageCollectionExtentTemporalCWProxy {
  OgcCoverageCollectionExtentTemporal interval(List<List<DateTime?>>? interval);

  OgcCoverageCollectionExtentTemporal values(List<String>? values);

  OgcCoverageCollectionExtentTemporal trs(String? trs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCoverageCollectionExtentTemporal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCoverageCollectionExtentTemporal(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcCoverageCollectionExtentTemporal call({
    List<List<DateTime?>>? interval,
    List<String>? values,
    String? trs,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcCoverageCollectionExtentTemporal.copyWith(...)` or call `instanceOfOgcCoverageCollectionExtentTemporal.copyWith.fieldName(value)` for a single field.
class _$OgcCoverageCollectionExtentTemporalCWProxyImpl
    implements _$OgcCoverageCollectionExtentTemporalCWProxy {
  const _$OgcCoverageCollectionExtentTemporalCWProxyImpl(this._value);

  final OgcCoverageCollectionExtentTemporal _value;

  @override
  OgcCoverageCollectionExtentTemporal interval(
    List<List<DateTime?>>? interval,
  ) => call(interval: interval);

  @override
  OgcCoverageCollectionExtentTemporal values(List<String>? values) =>
      call(values: values);

  @override
  OgcCoverageCollectionExtentTemporal trs(String? trs) => call(trs: trs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCoverageCollectionExtentTemporal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCoverageCollectionExtentTemporal(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcCoverageCollectionExtentTemporal call({
    Object? interval = const $CopyWithPlaceholder(),
    Object? values = const $CopyWithPlaceholder(),
    Object? trs = const $CopyWithPlaceholder(),
  }) {
    return OgcCoverageCollectionExtentTemporal(
      interval: interval == const $CopyWithPlaceholder()
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as List<List<DateTime?>>?,
      values: values == const $CopyWithPlaceholder()
          ? _value.values
          // ignore: cast_nullable_to_non_nullable
          : values as List<String>?,
      trs: trs == const $CopyWithPlaceholder()
          ? _value.trs
          // ignore: cast_nullable_to_non_nullable
          : trs as String?,
    );
  }
}

extension $OgcCoverageCollectionExtentTemporalCopyWith
    on OgcCoverageCollectionExtentTemporal {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcCoverageCollectionExtentTemporal.copyWith(...)` or `instanceOfOgcCoverageCollectionExtentTemporal.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcCoverageCollectionExtentTemporalCWProxy get copyWith =>
      _$OgcCoverageCollectionExtentTemporalCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcCoverageCollectionExtentTemporal
_$OgcCoverageCollectionExtentTemporalFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OgcCoverageCollectionExtentTemporal', json, (
      $checkedConvert,
    ) {
      final val = OgcCoverageCollectionExtentTemporal(
        interval: $checkedConvert(
          'interval',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => (e as List<dynamic>)
                    .map((e) => e == null ? null : DateTime.parse(e as String))
                    .toList(),
              )
              .toList(),
        ),
        values: $checkedConvert(
          'values',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        trs: $checkedConvert('trs', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$OgcCoverageCollectionExtentTemporalToJson(
  OgcCoverageCollectionExtentTemporal instance,
) => <String, dynamic>{
  'interval': ?instance.interval
      ?.map((e) => e.map((e) => e?.toIso8601String()).toList())
      .toList(),
  'values': ?instance.values,
  'trs': ?instance.trs,
};
