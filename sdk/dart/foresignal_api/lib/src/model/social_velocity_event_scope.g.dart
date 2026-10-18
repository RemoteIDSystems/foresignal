// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_velocity_event_scope.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialVelocityEventScopeCWProxy {
  SocialVelocityEventScope type(SocialVelocityEventScopeTypeEnum type);

  SocialVelocityEventScope id(String id);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVelocityEventScope(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVelocityEventScope(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialVelocityEventScope call({
    SocialVelocityEventScopeTypeEnum type,
    String id,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialVelocityEventScope.copyWith(...)` or call `instanceOfSocialVelocityEventScope.copyWith.fieldName(value)` for a single field.
class _$SocialVelocityEventScopeCWProxyImpl
    implements _$SocialVelocityEventScopeCWProxy {
  const _$SocialVelocityEventScopeCWProxyImpl(this._value);

  final SocialVelocityEventScope _value;

  @override
  SocialVelocityEventScope type(SocialVelocityEventScopeTypeEnum type) =>
      call(type: type);

  @override
  SocialVelocityEventScope id(String id) => call(id: id);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVelocityEventScope(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVelocityEventScope(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialVelocityEventScope call({
    Object? type = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
  }) {
    return SocialVelocityEventScope(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as SocialVelocityEventScopeTypeEnum,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
    );
  }
}

extension $SocialVelocityEventScopeCopyWith on SocialVelocityEventScope {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialVelocityEventScope.copyWith(...)` or `instanceOfSocialVelocityEventScope.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialVelocityEventScopeCWProxy get copyWith =>
      _$SocialVelocityEventScopeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialVelocityEventScope _$SocialVelocityEventScopeFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialVelocityEventScope', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type', 'id']);
  final val = SocialVelocityEventScope(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$SocialVelocityEventScopeTypeEnumEnumMap, v),
    ),
    id: $checkedConvert('id', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$SocialVelocityEventScopeToJson(
  SocialVelocityEventScope instance,
) => <String, dynamic>{
  'type': _$SocialVelocityEventScopeTypeEnumEnumMap[instance.type]!,
  'id': instance.id,
};

const _$SocialVelocityEventScopeTypeEnumEnumMap = {
  SocialVelocityEventScopeTypeEnum.topic: 'topic',
  SocialVelocityEventScopeTypeEnum.place: 'place',
  SocialVelocityEventScopeTypeEnum.entity: 'entity',
};
