// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'regional_index_detail.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegionalIndexDetailCWProxy {
  RegionalIndexDetail index(RegionalIndexDef index);

  RegionalIndexDetail region(String? region);

  RegionalIndexDetail from(DateTime? from);

  RegionalIndexDetail to(DateTime? to);

  RegionalIndexDetail values(List<RegionalIndexValue> values);

  RegionalIndexDetail latestFit(IndexFitResult? latestFit);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexDetail(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexDetail(...).copyWith(id: 12, name: "My name")
  /// ```
  RegionalIndexDetail call({
    RegionalIndexDef index,
    String? region,
    DateTime? from,
    DateTime? to,
    List<RegionalIndexValue> values,
    IndexFitResult? latestFit,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegionalIndexDetail.copyWith(...)` or call `instanceOfRegionalIndexDetail.copyWith.fieldName(value)` for a single field.
class _$RegionalIndexDetailCWProxyImpl implements _$RegionalIndexDetailCWProxy {
  const _$RegionalIndexDetailCWProxyImpl(this._value);

  final RegionalIndexDetail _value;

  @override
  RegionalIndexDetail index(RegionalIndexDef index) => call(index: index);

  @override
  RegionalIndexDetail region(String? region) => call(region: region);

  @override
  RegionalIndexDetail from(DateTime? from) => call(from: from);

  @override
  RegionalIndexDetail to(DateTime? to) => call(to: to);

  @override
  RegionalIndexDetail values(List<RegionalIndexValue> values) =>
      call(values: values);

  @override
  RegionalIndexDetail latestFit(IndexFitResult? latestFit) =>
      call(latestFit: latestFit);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexDetail(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexDetail(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegionalIndexDetail call({
    Object? index = const $CopyWithPlaceholder(),
    Object? region = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? values = const $CopyWithPlaceholder(),
    Object? latestFit = const $CopyWithPlaceholder(),
  }) {
    return RegionalIndexDetail(
      index: index == const $CopyWithPlaceholder() || index == null
          ? _value.index
          // ignore: cast_nullable_to_non_nullable
          : index as RegionalIndexDef,
      region: region == const $CopyWithPlaceholder()
          ? _value.region
          // ignore: cast_nullable_to_non_nullable
          : region as String?,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      values: values == const $CopyWithPlaceholder() || values == null
          ? _value.values
          // ignore: cast_nullable_to_non_nullable
          : values as List<RegionalIndexValue>,
      latestFit: latestFit == const $CopyWithPlaceholder()
          ? _value.latestFit
          // ignore: cast_nullable_to_non_nullable
          : latestFit as IndexFitResult?,
    );
  }
}

extension $RegionalIndexDetailCopyWith on RegionalIndexDetail {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegionalIndexDetail.copyWith(...)` or `instanceOfRegionalIndexDetail.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegionalIndexDetailCWProxy get copyWith =>
      _$RegionalIndexDetailCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegionalIndexDetail _$RegionalIndexDetailFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RegionalIndexDetail', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['index', 'values']);
  final val = RegionalIndexDetail(
    index: $checkedConvert(
      'index',
      (v) => RegionalIndexDef.fromJson(v as Map<String, dynamic>),
    ),
    region: $checkedConvert('region', (v) => v as String?),
    from: $checkedConvert(
      'from',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    to: $checkedConvert(
      'to',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    values: $checkedConvert(
      'values',
      (v) => (v as List<dynamic>)
          .map((e) => RegionalIndexValue.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    latestFit: $checkedConvert(
      'latestFit',
      (v) =>
          v == null ? null : IndexFitResult.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$RegionalIndexDetailToJson(
  RegionalIndexDetail instance,
) => <String, dynamic>{
  'index': instance.index.toJson(),
  'region': ?instance.region,
  'from': ?instance.from?.toIso8601String(),
  'to': ?instance.to?.toIso8601String(),
  'values': instance.values.map((e) => e.toJson()).toList(),
  'latestFit': ?instance.latestFit?.toJson(),
};
