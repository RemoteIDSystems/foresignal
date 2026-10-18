// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'coverage_json.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CoverageJsonCWProxy {
  CoverageJson type(CoverageJsonTypeEnum type);

  CoverageJson domain(Map<String, Object>? domain);

  CoverageJson parameters(Map<String, Map<String, Object>>? parameters);

  CoverageJson ranges(Map<String, Map<String, Object>>? ranges);

  CoverageJson coverages(List<Map<String, Object>>? coverages);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CoverageJson(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CoverageJson(...).copyWith(id: 12, name: "My name")
  /// ```
  CoverageJson call({
    CoverageJsonTypeEnum type,
    Map<String, Object>? domain,
    Map<String, Map<String, Object>>? parameters,
    Map<String, Map<String, Object>>? ranges,
    List<Map<String, Object>>? coverages,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCoverageJson.copyWith(...)` or call `instanceOfCoverageJson.copyWith.fieldName(value)` for a single field.
class _$CoverageJsonCWProxyImpl implements _$CoverageJsonCWProxy {
  const _$CoverageJsonCWProxyImpl(this._value);

  final CoverageJson _value;

  @override
  CoverageJson type(CoverageJsonTypeEnum type) => call(type: type);

  @override
  CoverageJson domain(Map<String, Object>? domain) => call(domain: domain);

  @override
  CoverageJson parameters(Map<String, Map<String, Object>>? parameters) =>
      call(parameters: parameters);

  @override
  CoverageJson ranges(Map<String, Map<String, Object>>? ranges) =>
      call(ranges: ranges);

  @override
  CoverageJson coverages(List<Map<String, Object>>? coverages) =>
      call(coverages: coverages);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CoverageJson(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CoverageJson(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CoverageJson call({
    Object? type = const $CopyWithPlaceholder(),
    Object? domain = const $CopyWithPlaceholder(),
    Object? parameters = const $CopyWithPlaceholder(),
    Object? ranges = const $CopyWithPlaceholder(),
    Object? coverages = const $CopyWithPlaceholder(),
  }) {
    return CoverageJson(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as CoverageJsonTypeEnum,
      domain: domain == const $CopyWithPlaceholder()
          ? _value.domain
          // ignore: cast_nullable_to_non_nullable
          : domain as Map<String, Object>?,
      parameters: parameters == const $CopyWithPlaceholder()
          ? _value.parameters
          // ignore: cast_nullable_to_non_nullable
          : parameters as Map<String, Map<String, Object>>?,
      ranges: ranges == const $CopyWithPlaceholder()
          ? _value.ranges
          // ignore: cast_nullable_to_non_nullable
          : ranges as Map<String, Map<String, Object>>?,
      coverages: coverages == const $CopyWithPlaceholder()
          ? _value.coverages
          // ignore: cast_nullable_to_non_nullable
          : coverages as List<Map<String, Object>>?,
    );
  }
}

extension $CoverageJsonCopyWith on CoverageJson {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCoverageJson.copyWith(...)` or `instanceOfCoverageJson.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CoverageJsonCWProxy get copyWith => _$CoverageJsonCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CoverageJson _$CoverageJsonFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CoverageJson', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type']);
  final val = CoverageJson(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$CoverageJsonTypeEnumEnumMap, v),
    ),
    domain: $checkedConvert(
      'domain',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    parameters: $checkedConvert(
      'parameters',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(
          k,
          (e as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
        ),
      ),
    ),
    ranges: $checkedConvert(
      'ranges',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(
          k,
          (e as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
        ),
      ),
    ),
    coverages: $checkedConvert(
      'coverages',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => (e as Map<String, dynamic>).map(
              (k, e) => MapEntry(k, e as Object),
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$CoverageJsonToJson(CoverageJson instance) =>
    <String, dynamic>{
      'type': _$CoverageJsonTypeEnumEnumMap[instance.type]!,
      'domain': ?instance.domain,
      'parameters': ?instance.parameters,
      'ranges': ?instance.ranges,
      'coverages': ?instance.coverages,
    };

const _$CoverageJsonTypeEnumEnumMap = {
  CoverageJsonTypeEnum.coverage: 'Coverage',
  CoverageJsonTypeEnum.coverageCollection: 'CoverageCollection',
};
