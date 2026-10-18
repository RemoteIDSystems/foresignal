// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_social_volume_selector_scopes_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSocialVolumeSelectorScopesInnerCWProxy {
  ChronicleSocialVolumeSelectorScopesInner type(
    ChronicleSocialVolumeSelectorScopesInnerTypeEnum type,
  );

  ChronicleSocialVolumeSelectorScopesInner id(String id);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSocialVolumeSelectorScopesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSocialVolumeSelectorScopesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSocialVolumeSelectorScopesInner call({
    ChronicleSocialVolumeSelectorScopesInnerTypeEnum type,
    String id,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSocialVolumeSelectorScopesInner.copyWith(...)` or call `instanceOfChronicleSocialVolumeSelectorScopesInner.copyWith.fieldName(value)` for a single field.
class _$ChronicleSocialVolumeSelectorScopesInnerCWProxyImpl
    implements _$ChronicleSocialVolumeSelectorScopesInnerCWProxy {
  const _$ChronicleSocialVolumeSelectorScopesInnerCWProxyImpl(this._value);

  final ChronicleSocialVolumeSelectorScopesInner _value;

  @override
  ChronicleSocialVolumeSelectorScopesInner type(
    ChronicleSocialVolumeSelectorScopesInnerTypeEnum type,
  ) => call(type: type);

  @override
  ChronicleSocialVolumeSelectorScopesInner id(String id) => call(id: id);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSocialVolumeSelectorScopesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSocialVolumeSelectorScopesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSocialVolumeSelectorScopesInner call({
    Object? type = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSocialVolumeSelectorScopesInner(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ChronicleSocialVolumeSelectorScopesInnerTypeEnum,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
    );
  }
}

extension $ChronicleSocialVolumeSelectorScopesInnerCopyWith
    on ChronicleSocialVolumeSelectorScopesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSocialVolumeSelectorScopesInner.copyWith(...)` or `instanceOfChronicleSocialVolumeSelectorScopesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSocialVolumeSelectorScopesInnerCWProxy get copyWith =>
      _$ChronicleSocialVolumeSelectorScopesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSocialVolumeSelectorScopesInner
_$ChronicleSocialVolumeSelectorScopesInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleSocialVolumeSelectorScopesInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['type', 'id']);
      final val = ChronicleSocialVolumeSelectorScopesInner(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(
            _$ChronicleSocialVolumeSelectorScopesInnerTypeEnumEnumMap,
            v,
          ),
        ),
        id: $checkedConvert('id', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleSocialVolumeSelectorScopesInnerToJson(
  ChronicleSocialVolumeSelectorScopesInner instance,
) => <String, dynamic>{
  'type':
      _$ChronicleSocialVolumeSelectorScopesInnerTypeEnumEnumMap[instance.type]!,
  'id': instance.id,
};

const _$ChronicleSocialVolumeSelectorScopesInnerTypeEnumEnumMap = {
  ChronicleSocialVolumeSelectorScopesInnerTypeEnum.topic: 'topic',
  ChronicleSocialVolumeSelectorScopesInnerTypeEnum.place: 'place',
  ChronicleSocialVolumeSelectorScopesInnerTypeEnum.entity: 'entity',
};
