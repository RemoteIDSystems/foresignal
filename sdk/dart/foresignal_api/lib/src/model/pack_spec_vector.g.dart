// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pack_spec_vector.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PackSpecVectorCWProxy {
  PackSpecVector sources(List<String>? sources);

  PackSpecVector kinds(List<String>? kinds);

  PackSpecVector asOf(DateTime? asOf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PackSpecVector(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PackSpecVector(...).copyWith(id: 12, name: "My name")
  /// ```
  PackSpecVector call({
    List<String>? sources,
    List<String>? kinds,
    DateTime? asOf,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPackSpecVector.copyWith(...)` or call `instanceOfPackSpecVector.copyWith.fieldName(value)` for a single field.
class _$PackSpecVectorCWProxyImpl implements _$PackSpecVectorCWProxy {
  const _$PackSpecVectorCWProxyImpl(this._value);

  final PackSpecVector _value;

  @override
  PackSpecVector sources(List<String>? sources) => call(sources: sources);

  @override
  PackSpecVector kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  PackSpecVector asOf(DateTime? asOf) => call(asOf: asOf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PackSpecVector(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PackSpecVector(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PackSpecVector call({
    Object? sources = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
  }) {
    return PackSpecVector(
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      kinds: kinds == const $CopyWithPlaceholder()
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>?,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
    );
  }
}

extension $PackSpecVectorCopyWith on PackSpecVector {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPackSpecVector.copyWith(...)` or `instanceOfPackSpecVector.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PackSpecVectorCWProxy get copyWith => _$PackSpecVectorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PackSpecVector _$PackSpecVectorFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PackSpecVector', json, ($checkedConvert) {
      final val = PackSpecVector(
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        kinds: $checkedConvert(
          'kinds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PackSpecVectorToJson(PackSpecVector instance) =>
    <String, dynamic>{
      'sources': ?instance.sources,
      'kinds': ?instance.kinds,
      'asOf': ?instance.asOf?.toIso8601String(),
    };
