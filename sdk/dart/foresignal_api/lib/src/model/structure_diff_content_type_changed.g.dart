// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'structure_diff_content_type_changed.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StructureDiffContentTypeChangedCWProxy {
  StructureDiffContentTypeChanged from(String from);

  StructureDiffContentTypeChanged to(String to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StructureDiffContentTypeChanged(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StructureDiffContentTypeChanged(...).copyWith(id: 12, name: "My name")
  /// ```
  StructureDiffContentTypeChanged call({String from, String to});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStructureDiffContentTypeChanged.copyWith(...)` or call `instanceOfStructureDiffContentTypeChanged.copyWith.fieldName(value)` for a single field.
class _$StructureDiffContentTypeChangedCWProxyImpl
    implements _$StructureDiffContentTypeChangedCWProxy {
  const _$StructureDiffContentTypeChangedCWProxyImpl(this._value);

  final StructureDiffContentTypeChanged _value;

  @override
  StructureDiffContentTypeChanged from(String from) => call(from: from);

  @override
  StructureDiffContentTypeChanged to(String to) => call(to: to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StructureDiffContentTypeChanged(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StructureDiffContentTypeChanged(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StructureDiffContentTypeChanged call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
  }) {
    return StructureDiffContentTypeChanged(
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

extension $StructureDiffContentTypeChangedCopyWith
    on StructureDiffContentTypeChanged {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStructureDiffContentTypeChanged.copyWith(...)` or `instanceOfStructureDiffContentTypeChanged.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StructureDiffContentTypeChangedCWProxy get copyWith =>
      _$StructureDiffContentTypeChangedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StructureDiffContentTypeChanged _$StructureDiffContentTypeChangedFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StructureDiffContentTypeChanged', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['from', 'to']);
  final val = StructureDiffContentTypeChanged(
    from: $checkedConvert('from', (v) => v as String),
    to: $checkedConvert('to', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$StructureDiffContentTypeChangedToJson(
  StructureDiffContentTypeChanged instance,
) => <String, dynamic>{'from': instance.from, 'to': instance.to};
