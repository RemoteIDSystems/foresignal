// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_collection_extent_temporal.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcCollectionExtentTemporalCWProxy {
  OgcCollectionExtentTemporal interval(List<List<DateTime?>>? interval);

  OgcCollectionExtentTemporal trs(String? trs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCollectionExtentTemporal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCollectionExtentTemporal(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcCollectionExtentTemporal call({
    List<List<DateTime?>>? interval,
    String? trs,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcCollectionExtentTemporal.copyWith(...)` or call `instanceOfOgcCollectionExtentTemporal.copyWith.fieldName(value)` for a single field.
class _$OgcCollectionExtentTemporalCWProxyImpl
    implements _$OgcCollectionExtentTemporalCWProxy {
  const _$OgcCollectionExtentTemporalCWProxyImpl(this._value);

  final OgcCollectionExtentTemporal _value;

  @override
  OgcCollectionExtentTemporal interval(List<List<DateTime?>>? interval) =>
      call(interval: interval);

  @override
  OgcCollectionExtentTemporal trs(String? trs) => call(trs: trs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCollectionExtentTemporal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCollectionExtentTemporal(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcCollectionExtentTemporal call({
    Object? interval = const $CopyWithPlaceholder(),
    Object? trs = const $CopyWithPlaceholder(),
  }) {
    return OgcCollectionExtentTemporal(
      interval: interval == const $CopyWithPlaceholder()
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as List<List<DateTime?>>?,
      trs: trs == const $CopyWithPlaceholder()
          ? _value.trs
          // ignore: cast_nullable_to_non_nullable
          : trs as String?,
    );
  }
}

extension $OgcCollectionExtentTemporalCopyWith on OgcCollectionExtentTemporal {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcCollectionExtentTemporal.copyWith(...)` or `instanceOfOgcCollectionExtentTemporal.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcCollectionExtentTemporalCWProxy get copyWith =>
      _$OgcCollectionExtentTemporalCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcCollectionExtentTemporal _$OgcCollectionExtentTemporalFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OgcCollectionExtentTemporal', json, ($checkedConvert) {
  final val = OgcCollectionExtentTemporal(
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
    trs: $checkedConvert('trs', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$OgcCollectionExtentTemporalToJson(
  OgcCollectionExtentTemporal instance,
) => <String, dynamic>{
  'interval': ?instance.interval
      ?.map((e) => e.map((e) => e?.toIso8601String()).toList())
      .toList(),
  'trs': ?instance.trs,
};
