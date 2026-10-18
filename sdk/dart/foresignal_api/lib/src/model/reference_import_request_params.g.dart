// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'reference_import_request_params.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReferenceImportRequestParamsCWProxy {
  ReferenceImportRequestParams file(String? file);

  ReferenceImportRequestParams useApi(bool? useApi);

  ReferenceImportRequestParams yearFrom(int? yearFrom);

  ReferenceImportRequestParams yearTo(int? yearTo);

  ReferenceImportRequestParams countries(List<String>? countries);

  ReferenceImportRequestParams minMagnitude(num? minMagnitude);

  ReferenceImportRequestParams hazardTypes(List<String>? hazardTypes);

  ReferenceImportRequestParams version(String? version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceImportRequestParams(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceImportRequestParams(...).copyWith(id: 12, name: "My name")
  /// ```
  ReferenceImportRequestParams call({
    String? file,
    bool? useApi,
    int? yearFrom,
    int? yearTo,
    List<String>? countries,
    num? minMagnitude,
    List<String>? hazardTypes,
    String? version,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReferenceImportRequestParams.copyWith(...)` or call `instanceOfReferenceImportRequestParams.copyWith.fieldName(value)` for a single field.
class _$ReferenceImportRequestParamsCWProxyImpl
    implements _$ReferenceImportRequestParamsCWProxy {
  const _$ReferenceImportRequestParamsCWProxyImpl(this._value);

  final ReferenceImportRequestParams _value;

  @override
  ReferenceImportRequestParams file(String? file) => call(file: file);

  @override
  ReferenceImportRequestParams useApi(bool? useApi) => call(useApi: useApi);

  @override
  ReferenceImportRequestParams yearFrom(int? yearFrom) =>
      call(yearFrom: yearFrom);

  @override
  ReferenceImportRequestParams yearTo(int? yearTo) => call(yearTo: yearTo);

  @override
  ReferenceImportRequestParams countries(List<String>? countries) =>
      call(countries: countries);

  @override
  ReferenceImportRequestParams minMagnitude(num? minMagnitude) =>
      call(minMagnitude: minMagnitude);

  @override
  ReferenceImportRequestParams hazardTypes(List<String>? hazardTypes) =>
      call(hazardTypes: hazardTypes);

  @override
  ReferenceImportRequestParams version(String? version) =>
      call(version: version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceImportRequestParams(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceImportRequestParams(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReferenceImportRequestParams call({
    Object? file = const $CopyWithPlaceholder(),
    Object? useApi = const $CopyWithPlaceholder(),
    Object? yearFrom = const $CopyWithPlaceholder(),
    Object? yearTo = const $CopyWithPlaceholder(),
    Object? countries = const $CopyWithPlaceholder(),
    Object? minMagnitude = const $CopyWithPlaceholder(),
    Object? hazardTypes = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
  }) {
    return ReferenceImportRequestParams(
      file: file == const $CopyWithPlaceholder()
          ? _value.file
          // ignore: cast_nullable_to_non_nullable
          : file as String?,
      useApi: useApi == const $CopyWithPlaceholder()
          ? _value.useApi
          // ignore: cast_nullable_to_non_nullable
          : useApi as bool?,
      yearFrom: yearFrom == const $CopyWithPlaceholder()
          ? _value.yearFrom
          // ignore: cast_nullable_to_non_nullable
          : yearFrom as int?,
      yearTo: yearTo == const $CopyWithPlaceholder()
          ? _value.yearTo
          // ignore: cast_nullable_to_non_nullable
          : yearTo as int?,
      countries: countries == const $CopyWithPlaceholder()
          ? _value.countries
          // ignore: cast_nullable_to_non_nullable
          : countries as List<String>?,
      minMagnitude: minMagnitude == const $CopyWithPlaceholder()
          ? _value.minMagnitude
          // ignore: cast_nullable_to_non_nullable
          : minMagnitude as num?,
      hazardTypes: hazardTypes == const $CopyWithPlaceholder()
          ? _value.hazardTypes
          // ignore: cast_nullable_to_non_nullable
          : hazardTypes as List<String>?,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String?,
    );
  }
}

extension $ReferenceImportRequestParamsCopyWith
    on ReferenceImportRequestParams {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReferenceImportRequestParams.copyWith(...)` or `instanceOfReferenceImportRequestParams.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReferenceImportRequestParamsCWProxy get copyWith =>
      _$ReferenceImportRequestParamsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReferenceImportRequestParams _$ReferenceImportRequestParamsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReferenceImportRequestParams', json, ($checkedConvert) {
  final val = ReferenceImportRequestParams(
    file: $checkedConvert('file', (v) => v as String?),
    useApi: $checkedConvert('useApi', (v) => v as bool?),
    yearFrom: $checkedConvert('yearFrom', (v) => (v as num?)?.toInt()),
    yearTo: $checkedConvert('yearTo', (v) => (v as num?)?.toInt()),
    countries: $checkedConvert(
      'countries',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    minMagnitude: $checkedConvert('minMagnitude', (v) => v as num? ?? 4.5),
    hazardTypes: $checkedConvert(
      'hazardTypes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    version: $checkedConvert('version', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ReferenceImportRequestParamsToJson(
  ReferenceImportRequestParams instance,
) => <String, dynamic>{
  'file': ?instance.file,
  'useApi': ?instance.useApi,
  'yearFrom': ?instance.yearFrom,
  'yearTo': ?instance.yearTo,
  'countries': ?instance.countries,
  'minMagnitude': ?instance.minMagnitude,
  'hazardTypes': ?instance.hazardTypes,
  'version': ?instance.version,
};
