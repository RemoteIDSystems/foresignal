// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'shared_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SharedItemCWProxy {
  SharedItem envelope(ObjectEnvelope envelope);

  SharedItem role(SharedItemRoleEnum role);

  SharedItem via(SharedItemViaEnum via);

  SharedItem groupId(String? groupId);

  SharedItem sharedBy(PrincipalSummary? sharedBy);

  SharedItem sharedAt(DateTime sharedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SharedItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SharedItem(...).copyWith(id: 12, name: "My name")
  /// ```
  SharedItem call({
    ObjectEnvelope envelope,
    SharedItemRoleEnum role,
    SharedItemViaEnum via,
    String? groupId,
    PrincipalSummary? sharedBy,
    DateTime sharedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSharedItem.copyWith(...)` or call `instanceOfSharedItem.copyWith.fieldName(value)` for a single field.
class _$SharedItemCWProxyImpl implements _$SharedItemCWProxy {
  const _$SharedItemCWProxyImpl(this._value);

  final SharedItem _value;

  @override
  SharedItem envelope(ObjectEnvelope envelope) => call(envelope: envelope);

  @override
  SharedItem role(SharedItemRoleEnum role) => call(role: role);

  @override
  SharedItem via(SharedItemViaEnum via) => call(via: via);

  @override
  SharedItem groupId(String? groupId) => call(groupId: groupId);

  @override
  SharedItem sharedBy(PrincipalSummary? sharedBy) => call(sharedBy: sharedBy);

  @override
  SharedItem sharedAt(DateTime sharedAt) => call(sharedAt: sharedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SharedItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SharedItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SharedItem call({
    Object? envelope = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? via = const $CopyWithPlaceholder(),
    Object? groupId = const $CopyWithPlaceholder(),
    Object? sharedBy = const $CopyWithPlaceholder(),
    Object? sharedAt = const $CopyWithPlaceholder(),
  }) {
    return SharedItem(
      envelope: envelope == const $CopyWithPlaceholder() || envelope == null
          ? _value.envelope
          // ignore: cast_nullable_to_non_nullable
          : envelope as ObjectEnvelope,
      role: role == const $CopyWithPlaceholder() || role == null
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as SharedItemRoleEnum,
      via: via == const $CopyWithPlaceholder() || via == null
          ? _value.via
          // ignore: cast_nullable_to_non_nullable
          : via as SharedItemViaEnum,
      groupId: groupId == const $CopyWithPlaceholder()
          ? _value.groupId
          // ignore: cast_nullable_to_non_nullable
          : groupId as String?,
      sharedBy: sharedBy == const $CopyWithPlaceholder()
          ? _value.sharedBy
          // ignore: cast_nullable_to_non_nullable
          : sharedBy as PrincipalSummary?,
      sharedAt: sharedAt == const $CopyWithPlaceholder() || sharedAt == null
          ? _value.sharedAt
          // ignore: cast_nullable_to_non_nullable
          : sharedAt as DateTime,
    );
  }
}

extension $SharedItemCopyWith on SharedItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSharedItem.copyWith(...)` or `instanceOfSharedItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SharedItemCWProxy get copyWith => _$SharedItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SharedItem _$SharedItemFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SharedItem', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['envelope', 'role', 'via', 'sharedAt']);
  final val = SharedItem(
    envelope: $checkedConvert(
      'envelope',
      (v) => ObjectEnvelope.fromJson(v as Map<String, dynamic>),
    ),
    role: $checkedConvert(
      'role',
      (v) => $enumDecode(_$SharedItemRoleEnumEnumMap, v),
    ),
    via: $checkedConvert(
      'via',
      (v) => $enumDecode(_$SharedItemViaEnumEnumMap, v),
    ),
    groupId: $checkedConvert('groupId', (v) => v as String?),
    sharedBy: $checkedConvert(
      'sharedBy',
      (v) => v == null
          ? null
          : PrincipalSummary.fromJson(v as Map<String, dynamic>),
    ),
    sharedAt: $checkedConvert('sharedAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$SharedItemToJson(SharedItem instance) =>
    <String, dynamic>{
      'envelope': instance.envelope.toJson(),
      'role': _$SharedItemRoleEnumEnumMap[instance.role]!,
      'via': _$SharedItemViaEnumEnumMap[instance.via]!,
      'groupId': ?instance.groupId,
      'sharedBy': ?instance.sharedBy?.toJson(),
      'sharedAt': instance.sharedAt.toIso8601String(),
    };

const _$SharedItemRoleEnumEnumMap = {
  SharedItemRoleEnum.viewer: 'viewer',
  SharedItemRoleEnum.commenter: 'commenter',
  SharedItemRoleEnum.editor: 'editor',
  SharedItemRoleEnum.owner: 'owner',
};

const _$SharedItemViaEnumEnumMap = {
  SharedItemViaEnum.principal: 'principal',
  SharedItemViaEnum.group: 'group',
  SharedItemViaEnum.instance: 'instance',
};
