// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'structure_diff_type_changed_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StructureDiffTypeChangedInnerCWProxy {
  StructureDiffTypeChangedInner path(String path);

  StructureDiffTypeChangedInner from(String from);

  StructureDiffTypeChangedInner to(String to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StructureDiffTypeChangedInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StructureDiffTypeChangedInner(...).copyWith(id: 12, name: "My name")
  /// ```
  StructureDiffTypeChangedInner call({String path, String from, String to});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStructureDiffTypeChangedInner.copyWith(...)` or call `instanceOfStructureDiffTypeChangedInner.copyWith.fieldName(value)` for a single field.
class _$StructureDiffTypeChangedInnerCWProxyImpl
    implements _$StructureDiffTypeChangedInnerCWProxy {
  const _$StructureDiffTypeChangedInnerCWProxyImpl(this._value);

  final StructureDiffTypeChangedInner _value;

  @override
  StructureDiffTypeChangedInner path(String path) => call(path: path);

  @override
  StructureDiffTypeChangedInner from(String from) => call(from: from);

  @override
  StructureDiffTypeChangedInner to(String to) => call(to: to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StructureDiffTypeChangedInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StructureDiffTypeChangedInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StructureDiffTypeChangedInner call({
    Object? path = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
  }) {
    return StructureDiffTypeChangedInner(
      path: path == const $CopyWithPlaceholder() || path == null
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String,
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as String,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as String,
    );
  }
}

extension $StructureDiffTypeChangedInnerCopyWith
    on StructureDiffTypeChangedInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStructureDiffTypeChangedInner.copyWith(...)` or `instanceOfStructureDiffTypeChangedInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StructureDiffTypeChangedInnerCWProxy get copyWith =>
      _$StructureDiffTypeChangedInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StructureDiffTypeChangedInner _$StructureDiffTypeChangedInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StructureDiffTypeChangedInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['path', 'from', 'to']);
  final val = StructureDiffTypeChangedInner(
    path: $checkedConvert('path', (v) => v as String),
    from: $checkedConvert('from', (v) => v as String),
    to: $checkedConvert('to', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$StructureDiffTypeChangedInnerToJson(
  StructureDiffTypeChangedInner instance,
) => <String, dynamic>{
  'path': instance.path,
  'from': instance.from,
  'to': instance.to,
};
