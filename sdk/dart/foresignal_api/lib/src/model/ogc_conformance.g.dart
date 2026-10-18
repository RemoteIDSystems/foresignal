// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_conformance.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcConformanceCWProxy {
  OgcConformance conformsTo(List<String> conformsTo);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcConformance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcConformance(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcConformance call({List<String> conformsTo});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcConformance.copyWith(...)` or call `instanceOfOgcConformance.copyWith.fieldName(value)` for a single field.
class _$OgcConformanceCWProxyImpl implements _$OgcConformanceCWProxy {
  const _$OgcConformanceCWProxyImpl(this._value);

  final OgcConformance _value;

  @override
  OgcConformance conformsTo(List<String> conformsTo) =>
      call(conformsTo: conformsTo);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcConformance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcConformance(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcConformance call({Object? conformsTo = const $CopyWithPlaceholder()}) {
    return OgcConformance(
      conformsTo:
          conformsTo == const $CopyWithPlaceholder() || conformsTo == null
          ? _value.conformsTo
          // ignore: cast_nullable_to_non_nullable
          : conformsTo as List<String>,
    );
  }
}

extension $OgcConformanceCopyWith on OgcConformance {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcConformance.copyWith(...)` or `instanceOfOgcConformance.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcConformanceCWProxy get copyWith => _$OgcConformanceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcConformance _$OgcConformanceFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OgcConformance', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['conformsTo']);
      final val = OgcConformance(
        conformsTo: $checkedConvert(
          'conformsTo',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OgcConformanceToJson(OgcConformance instance) =>
    <String, dynamic>{'conformsTo': instance.conformsTo};
