// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_delete_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UserDeleteResultCWProxy {
  UserDeleteResult deletedUserId(String deletedUserId);

  UserDeleteResult ownedConfigDisposition(String ownedConfigDisposition);

  UserDeleteResult reassignedTo(String? reassignedTo);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserDeleteResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserDeleteResult(...).copyWith(id: 12, name: "My name")
  /// ```
  UserDeleteResult call({
    String deletedUserId,
    String ownedConfigDisposition,
    String? reassignedTo,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfUserDeleteResult.copyWith(...)` or call `instanceOfUserDeleteResult.copyWith.fieldName(value)` for a single field.
class _$UserDeleteResultCWProxyImpl implements _$UserDeleteResultCWProxy {
  const _$UserDeleteResultCWProxyImpl(this._value);

  final UserDeleteResult _value;

  @override
  UserDeleteResult deletedUserId(String deletedUserId) =>
      call(deletedUserId: deletedUserId);

  @override
  UserDeleteResult ownedConfigDisposition(String ownedConfigDisposition) =>
      call(ownedConfigDisposition: ownedConfigDisposition);

  @override
  UserDeleteResult reassignedTo(String? reassignedTo) =>
      call(reassignedTo: reassignedTo);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserDeleteResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserDeleteResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  UserDeleteResult call({
    Object? deletedUserId = const $CopyWithPlaceholder(),
    Object? ownedConfigDisposition = const $CopyWithPlaceholder(),
    Object? reassignedTo = const $CopyWithPlaceholder(),
  }) {
    return UserDeleteResult(
      deletedUserId:
          deletedUserId == const $CopyWithPlaceholder() || deletedUserId == null
          ? _value.deletedUserId
          // ignore: cast_nullable_to_non_nullable
          : deletedUserId as String,
      ownedConfigDisposition:
          ownedConfigDisposition == const $CopyWithPlaceholder() ||
              ownedConfigDisposition == null
          ? _value.ownedConfigDisposition
          // ignore: cast_nullable_to_non_nullable
          : ownedConfigDisposition as String,
      reassignedTo: reassignedTo == const $CopyWithPlaceholder()
          ? _value.reassignedTo
          // ignore: cast_nullable_to_non_nullable
          : reassignedTo as String?,
    );
  }
}

extension $UserDeleteResultCopyWith on UserDeleteResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfUserDeleteResult.copyWith(...)` or `instanceOfUserDeleteResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UserDeleteResultCWProxy get copyWith => _$UserDeleteResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UserDeleteResult _$UserDeleteResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('UserDeleteResult', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['deletedUserId', 'ownedConfigDisposition'],
      );
      final val = UserDeleteResult(
        deletedUserId: $checkedConvert('deletedUserId', (v) => v as String),
        ownedConfigDisposition: $checkedConvert(
          'ownedConfigDisposition',
          (v) => v as String,
        ),
        reassignedTo: $checkedConvert('reassignedTo', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$UserDeleteResultToJson(UserDeleteResult instance) =>
    <String, dynamic>{
      'deletedUserId': instance.deletedUserId,
      'ownedConfigDisposition': instance.ownedConfigDisposition,
      'reassignedTo': ?instance.reassignedTo,
    };
