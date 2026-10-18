// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'field_manifest_components_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FieldManifestComponentsInnerCWProxy {
  FieldManifestComponentsInner name(FieldManifestComponentsInnerNameEnum name);

  FieldManifestComponentsInner offset(int offset);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldManifestComponentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldManifestComponentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  FieldManifestComponentsInner call({
    FieldManifestComponentsInnerNameEnum name,
    int offset,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFieldManifestComponentsInner.copyWith(...)` or call `instanceOfFieldManifestComponentsInner.copyWith.fieldName(value)` for a single field.
class _$FieldManifestComponentsInnerCWProxyImpl
    implements _$FieldManifestComponentsInnerCWProxy {
  const _$FieldManifestComponentsInnerCWProxyImpl(this._value);

  final FieldManifestComponentsInner _value;

  @override
  FieldManifestComponentsInner name(
    FieldManifestComponentsInnerNameEnum name,
  ) => call(name: name);

  @override
  FieldManifestComponentsInner offset(int offset) => call(offset: offset);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldManifestComponentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldManifestComponentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FieldManifestComponentsInner call({
    Object? name = const $CopyWithPlaceholder(),
    Object? offset = const $CopyWithPlaceholder(),
  }) {
    return FieldManifestComponentsInner(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as FieldManifestComponentsInnerNameEnum,
      offset: offset == const $CopyWithPlaceholder() || offset == null
          ? _value.offset
          // ignore: cast_nullable_to_non_nullable
          : offset as int,
    );
  }
}

extension $FieldManifestComponentsInnerCopyWith
    on FieldManifestComponentsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFieldManifestComponentsInner.copyWith(...)` or `instanceOfFieldManifestComponentsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FieldManifestComponentsInnerCWProxy get copyWith =>
      _$FieldManifestComponentsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FieldManifestComponentsInner _$FieldManifestComponentsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('FieldManifestComponentsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['name', 'offset']);
  final val = FieldManifestComponentsInner(
    name: $checkedConvert(
      'name',
      (v) => $enumDecode(_$FieldManifestComponentsInnerNameEnumEnumMap, v),
    ),
    offset: $checkedConvert('offset', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$FieldManifestComponentsInnerToJson(
  FieldManifestComponentsInner instance,
) => <String, dynamic>{
  'name': _$FieldManifestComponentsInnerNameEnumEnumMap[instance.name]!,
  'offset': instance.offset,
};

const _$FieldManifestComponentsInnerNameEnumEnumMap = {
  FieldManifestComponentsInnerNameEnum.u: 'u',
  FieldManifestComponentsInnerNameEnum.v: 'v',
};
