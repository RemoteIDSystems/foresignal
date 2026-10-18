// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'object_acl_entry.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ObjectAclEntryCWProxy {
  ObjectAclEntry grantee(String grantee);

  ObjectAclEntry granteeDisplay(String? granteeDisplay);

  ObjectAclEntry role(ObjectAclEntryRoleEnum role);

  ObjectAclEntry grantedBy(PrincipalSummary? grantedBy);

  ObjectAclEntry grantedAt(DateTime grantedAt);

  ObjectAclEntry expiresAt(DateTime? expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectAclEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectAclEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  ObjectAclEntry call({
    String grantee,
    String? granteeDisplay,
    ObjectAclEntryRoleEnum role,
    PrincipalSummary? grantedBy,
    DateTime grantedAt,
    DateTime? expiresAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfObjectAclEntry.copyWith(...)` or call `instanceOfObjectAclEntry.copyWith.fieldName(value)` for a single field.
class _$ObjectAclEntryCWProxyImpl implements _$ObjectAclEntryCWProxy {
  const _$ObjectAclEntryCWProxyImpl(this._value);

  final ObjectAclEntry _value;

  @override
  ObjectAclEntry grantee(String grantee) => call(grantee: grantee);

  @override
  ObjectAclEntry granteeDisplay(String? granteeDisplay) =>
      call(granteeDisplay: granteeDisplay);

  @override
  ObjectAclEntry role(ObjectAclEntryRoleEnum role) => call(role: role);

  @override
  ObjectAclEntry grantedBy(PrincipalSummary? grantedBy) =>
      call(grantedBy: grantedBy);

  @override
  ObjectAclEntry grantedAt(DateTime grantedAt) => call(grantedAt: grantedAt);

  @override
  ObjectAclEntry expiresAt(DateTime? expiresAt) => call(expiresAt: expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectAclEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectAclEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ObjectAclEntry call({
    Object? grantee = const $CopyWithPlaceholder(),
    Object? granteeDisplay = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? grantedBy = const $CopyWithPlaceholder(),
    Object? grantedAt = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
  }) {
    return ObjectAclEntry(
      grantee: grantee == const $CopyWithPlaceholder() || grantee == null
          ? _value.grantee
          // ignore: cast_nullable_to_non_nullable
          : grantee as String,
      granteeDisplay: granteeDisplay == const $CopyWithPlaceholder()
          ? _value.granteeDisplay
          // ignore: cast_nullable_to_non_nullable
          : granteeDisplay as String?,
      role: role == const $CopyWithPlaceholder() || role == null
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as ObjectAclEntryRoleEnum,
      grantedBy: grantedBy == const $CopyWithPlaceholder()
          ? _value.grantedBy
          // ignore: cast_nullable_to_non_nullable
          : grantedBy as PrincipalSummary?,
      grantedAt: grantedAt == const $CopyWithPlaceholder() || grantedAt == null
          ? _value.grantedAt
          // ignore: cast_nullable_to_non_nullable
          : grantedAt as DateTime,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
    );
  }
}

extension $ObjectAclEntryCopyWith on ObjectAclEntry {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfObjectAclEntry.copyWith(...)` or `instanceOfObjectAclEntry.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ObjectAclEntryCWProxy get copyWith => _$ObjectAclEntryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ObjectAclEntry _$ObjectAclEntryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ObjectAclEntry', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['grantee', 'role', 'grantedAt']);
      final val = ObjectAclEntry(
        grantee: $checkedConvert('grantee', (v) => v as String),
        granteeDisplay: $checkedConvert('granteeDisplay', (v) => v as String?),
        role: $checkedConvert(
          'role',
          (v) => $enumDecode(_$ObjectAclEntryRoleEnumEnumMap, v),
        ),
        grantedBy: $checkedConvert(
          'grantedBy',
          (v) => v == null
              ? null
              : PrincipalSummary.fromJson(v as Map<String, dynamic>),
        ),
        grantedAt: $checkedConvert(
          'grantedAt',
          (v) => DateTime.parse(v as String),
        ),
        expiresAt: $checkedConvert(
          'expiresAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ObjectAclEntryToJson(ObjectAclEntry instance) =>
    <String, dynamic>{
      'grantee': instance.grantee,
      'granteeDisplay': ?instance.granteeDisplay,
      'role': _$ObjectAclEntryRoleEnumEnumMap[instance.role]!,
      'grantedBy': ?instance.grantedBy?.toJson(),
      'grantedAt': instance.grantedAt.toIso8601String(),
      'expiresAt': ?instance.expiresAt?.toIso8601String(),
    };

const _$ObjectAclEntryRoleEnumEnumMap = {
  ObjectAclEntryRoleEnum.viewer: 'viewer',
  ObjectAclEntryRoleEnum.commenter: 'commenter',
  ObjectAclEntryRoleEnum.editor: 'editor',
  ObjectAclEntryRoleEnum.owner: 'owner',
};
