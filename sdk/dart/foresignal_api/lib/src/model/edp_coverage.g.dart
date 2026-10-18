// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_coverage.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpCoverageCWProxy {
  EdpCoverage area(EdpGeometry? area);

  EdpCoverage family(String? family);

  EdpCoverage pDetect(num? pDetect);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpCoverage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpCoverage(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpCoverage call({EdpGeometry? area, String? family, num? pDetect});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpCoverage.copyWith(...)` or call `instanceOfEdpCoverage.copyWith.fieldName(value)` for a single field.
class _$EdpCoverageCWProxyImpl implements _$EdpCoverageCWProxy {
  const _$EdpCoverageCWProxyImpl(this._value);

  final EdpCoverage _value;

  @override
  EdpCoverage area(EdpGeometry? area) => call(area: area);

  @override
  EdpCoverage family(String? family) => call(family: family);

  @override
  EdpCoverage pDetect(num? pDetect) => call(pDetect: pDetect);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpCoverage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpCoverage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpCoverage call({
    Object? area = const $CopyWithPlaceholder(),
    Object? family = const $CopyWithPlaceholder(),
    Object? pDetect = const $CopyWithPlaceholder(),
  }) {
    return EdpCoverage(
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as EdpGeometry?,
      family: family == const $CopyWithPlaceholder()
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String?,
      pDetect: pDetect == const $CopyWithPlaceholder()
          ? _value.pDetect
          // ignore: cast_nullable_to_non_nullable
          : pDetect as num?,
    );
  }
}

extension $EdpCoverageCopyWith on EdpCoverage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpCoverage.copyWith(...)` or `instanceOfEdpCoverage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpCoverageCWProxy get copyWith => _$EdpCoverageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpCoverage _$EdpCoverageFromJson(Map<String, dynamic> json) => $checkedCreate(
  'EdpCoverage',
  json,
  ($checkedConvert) {
    final val = EdpCoverage(
      area: $checkedConvert(
        'area',
        (v) =>
            v == null ? null : EdpGeometry.fromJson(v as Map<String, dynamic>),
      ),
      family: $checkedConvert('family', (v) => v as String?),
      pDetect: $checkedConvert('pDetect', (v) => v as num?),
    );
    return val;
  },
);

Map<String, dynamic> _$EdpCoverageToJson(EdpCoverage instance) =>
    <String, dynamic>{
      'area': ?instance.area?.toJson(),
      'family': ?instance.family,
      'pDetect': ?instance.pDetect,
    };
