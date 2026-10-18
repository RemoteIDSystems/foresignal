// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_volume_scope.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialVolumeScopeCWProxy {
  SocialVolumeScope type(SocialVolumeScopeTypeEnum type);

  SocialVolumeScope id(String id);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVolumeScope(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVolumeScope(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialVolumeScope call({SocialVolumeScopeTypeEnum type, String id});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialVolumeScope.copyWith(...)` or call `instanceOfSocialVolumeScope.copyWith.fieldName(value)` for a single field.
class _$SocialVolumeScopeCWProxyImpl implements _$SocialVolumeScopeCWProxy {
  const _$SocialVolumeScopeCWProxyImpl(this._value);

  final SocialVolumeScope _value;

  @override
  SocialVolumeScope type(SocialVolumeScopeTypeEnum type) => call(type: type);

  @override
  SocialVolumeScope id(String id) => call(id: id);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVolumeScope(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVolumeScope(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialVolumeScope call({
    Object? type = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
  }) {
    return SocialVolumeScope(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as SocialVolumeScopeTypeEnum,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
    );
  }
}

extension $SocialVolumeScopeCopyWith on SocialVolumeScope {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialVolumeScope.copyWith(...)` or `instanceOfSocialVolumeScope.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialVolumeScopeCWProxy get copyWith =>
      _$SocialVolumeScopeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialVolumeScope _$SocialVolumeScopeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialVolumeScope', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'id']);
      final val = SocialVolumeScope(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$SocialVolumeScopeTypeEnumEnumMap, v),
        ),
        id: $checkedConvert('id', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$SocialVolumeScopeToJson(SocialVolumeScope instance) =>
    <String, dynamic>{
      'type': _$SocialVolumeScopeTypeEnumEnumMap[instance.type]!,
      'id': instance.id,
    };

const _$SocialVolumeScopeTypeEnumEnumMap = {
  SocialVolumeScopeTypeEnum.topic: 'topic',
  SocialVolumeScopeTypeEnum.place: 'place',
  SocialVolumeScopeTypeEnum.entity: 'entity',
};
