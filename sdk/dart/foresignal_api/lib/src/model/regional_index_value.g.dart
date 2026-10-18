// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'regional_index_value.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegionalIndexValueCWProxy {
  RegionalIndexValue indexId(String indexId);

  RegionalIndexValue region(String region);

  RegionalIndexValue day(DateTime day);

  RegionalIndexValue value(num value);

  RegionalIndexValue components(
    List<RegionalIndexValueComponentsInner> components,
  );

  RegionalIndexValue floorsApplied(List<String>? floorsApplied);

  RegionalIndexValue delta24h(num? delta24h);

  RegionalIndexValue delta7d(num? delta7d);

  RegionalIndexValue weightsVersion(int? weightsVersion);

  RegionalIndexValue asOfSeq(int asOfSeq);

  RegionalIndexValue manifestId(String? manifestId);

  RegionalIndexValue recordRef(String? recordRef);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexValue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexValue(...).copyWith(id: 12, name: "My name")
  /// ```
  RegionalIndexValue call({
    String indexId,
    String region,
    DateTime day,
    num value,
    List<RegionalIndexValueComponentsInner> components,
    List<String>? floorsApplied,
    num? delta24h,
    num? delta7d,
    int? weightsVersion,
    int asOfSeq,
    String? manifestId,
    String? recordRef,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegionalIndexValue.copyWith(...)` or call `instanceOfRegionalIndexValue.copyWith.fieldName(value)` for a single field.
class _$RegionalIndexValueCWProxyImpl implements _$RegionalIndexValueCWProxy {
  const _$RegionalIndexValueCWProxyImpl(this._value);

  final RegionalIndexValue _value;

  @override
  RegionalIndexValue indexId(String indexId) => call(indexId: indexId);

  @override
  RegionalIndexValue region(String region) => call(region: region);

  @override
  RegionalIndexValue day(DateTime day) => call(day: day);

  @override
  RegionalIndexValue value(num value) => call(value: value);

  @override
  RegionalIndexValue components(
    List<RegionalIndexValueComponentsInner> components,
  ) => call(components: components);

  @override
  RegionalIndexValue floorsApplied(List<String>? floorsApplied) =>
      call(floorsApplied: floorsApplied);

  @override
  RegionalIndexValue delta24h(num? delta24h) => call(delta24h: delta24h);

  @override
  RegionalIndexValue delta7d(num? delta7d) => call(delta7d: delta7d);

  @override
  RegionalIndexValue weightsVersion(int? weightsVersion) =>
      call(weightsVersion: weightsVersion);

  @override
  RegionalIndexValue asOfSeq(int asOfSeq) => call(asOfSeq: asOfSeq);

  @override
  RegionalIndexValue manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  @override
  RegionalIndexValue recordRef(String? recordRef) => call(recordRef: recordRef);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexValue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexValue(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegionalIndexValue call({
    Object? indexId = const $CopyWithPlaceholder(),
    Object? region = const $CopyWithPlaceholder(),
    Object? day = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? components = const $CopyWithPlaceholder(),
    Object? floorsApplied = const $CopyWithPlaceholder(),
    Object? delta24h = const $CopyWithPlaceholder(),
    Object? delta7d = const $CopyWithPlaceholder(),
    Object? weightsVersion = const $CopyWithPlaceholder(),
    Object? asOfSeq = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? recordRef = const $CopyWithPlaceholder(),
  }) {
    return RegionalIndexValue(
      indexId: indexId == const $CopyWithPlaceholder() || indexId == null
          ? _value.indexId
          // ignore: cast_nullable_to_non_nullable
          : indexId as String,
      region: region == const $CopyWithPlaceholder() || region == null
          ? _value.region
          // ignore: cast_nullable_to_non_nullable
          : region as String,
      day: day == const $CopyWithPlaceholder() || day == null
          ? _value.day
          // ignore: cast_nullable_to_non_nullable
          : day as DateTime,
      value: value == const $CopyWithPlaceholder() || value == null
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num,
      components:
          components == const $CopyWithPlaceholder() || components == null
          ? _value.components
          // ignore: cast_nullable_to_non_nullable
          : components as List<RegionalIndexValueComponentsInner>,
      floorsApplied: floorsApplied == const $CopyWithPlaceholder()
          ? _value.floorsApplied
          // ignore: cast_nullable_to_non_nullable
          : floorsApplied as List<String>?,
      delta24h: delta24h == const $CopyWithPlaceholder()
          ? _value.delta24h
          // ignore: cast_nullable_to_non_nullable
          : delta24h as num?,
      delta7d: delta7d == const $CopyWithPlaceholder()
          ? _value.delta7d
          // ignore: cast_nullable_to_non_nullable
          : delta7d as num?,
      weightsVersion: weightsVersion == const $CopyWithPlaceholder()
          ? _value.weightsVersion
          // ignore: cast_nullable_to_non_nullable
          : weightsVersion as int?,
      asOfSeq: asOfSeq == const $CopyWithPlaceholder() || asOfSeq == null
          ? _value.asOfSeq
          // ignore: cast_nullable_to_non_nullable
          : asOfSeq as int,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
      recordRef: recordRef == const $CopyWithPlaceholder()
          ? _value.recordRef
          // ignore: cast_nullable_to_non_nullable
          : recordRef as String?,
    );
  }
}

extension $RegionalIndexValueCopyWith on RegionalIndexValue {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegionalIndexValue.copyWith(...)` or `instanceOfRegionalIndexValue.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegionalIndexValueCWProxy get copyWith =>
      _$RegionalIndexValueCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegionalIndexValue _$RegionalIndexValueFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RegionalIndexValue', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'indexId',
          'region',
          'day',
          'value',
          'components',
          'asOfSeq',
        ],
      );
      final val = RegionalIndexValue(
        indexId: $checkedConvert('indexId', (v) => v as String),
        region: $checkedConvert('region', (v) => v as String),
        day: $checkedConvert('day', (v) => DateTime.parse(v as String)),
        value: $checkedConvert('value', (v) => v as num),
        components: $checkedConvert(
          'components',
          (v) => (v as List<dynamic>)
              .map(
                (e) => RegionalIndexValueComponentsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        floorsApplied: $checkedConvert(
          'floorsApplied',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        delta24h: $checkedConvert('delta24h', (v) => v as num?),
        delta7d: $checkedConvert('delta7d', (v) => v as num?),
        weightsVersion: $checkedConvert(
          'weightsVersion',
          (v) => (v as num?)?.toInt(),
        ),
        asOfSeq: $checkedConvert('asOfSeq', (v) => (v as num).toInt()),
        manifestId: $checkedConvert('manifestId', (v) => v as String?),
        recordRef: $checkedConvert('recordRef', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$RegionalIndexValueToJson(RegionalIndexValue instance) =>
    <String, dynamic>{
      'indexId': instance.indexId,
      'region': instance.region,
      'day': instance.day.toIso8601String(),
      'value': instance.value,
      'components': instance.components.map((e) => e.toJson()).toList(),
      'floorsApplied': ?instance.floorsApplied,
      'delta24h': ?instance.delta24h,
      'delta7d': ?instance.delta7d,
      'weightsVersion': ?instance.weightsVersion,
      'asOfSeq': instance.asOfSeq,
      'manifestId': ?instance.manifestId,
      'recordRef': ?instance.recordRef,
    };
