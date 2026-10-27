// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'backup_restore_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BackupRestoreRequestCWProxy {
  BackupRestoreRequest confirm(bool confirm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BackupRestoreRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BackupRestoreRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  BackupRestoreRequest call({bool confirm});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBackupRestoreRequest.copyWith(...)` or call `instanceOfBackupRestoreRequest.copyWith.fieldName(value)` for a single field.
class _$BackupRestoreRequestCWProxyImpl
    implements _$BackupRestoreRequestCWProxy {
  const _$BackupRestoreRequestCWProxyImpl(this._value);

  final BackupRestoreRequest _value;

  @override
  BackupRestoreRequest confirm(bool confirm) => call(confirm: confirm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BackupRestoreRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BackupRestoreRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BackupRestoreRequest call({Object? confirm = const $CopyWithPlaceholder()}) {
    return BackupRestoreRequest(
      confirm: confirm == const $CopyWithPlaceholder() || confirm == null
          ? _value.confirm
          // ignore: cast_nullable_to_non_nullable
          : confirm as bool,
    );
  }
}

extension $BackupRestoreRequestCopyWith on BackupRestoreRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBackupRestoreRequest.copyWith(...)` or `instanceOfBackupRestoreRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BackupRestoreRequestCWProxy get copyWith =>
      _$BackupRestoreRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BackupRestoreRequest _$BackupRestoreRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('BackupRestoreRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['confirm']);
  final val = BackupRestoreRequest(
    confirm: $checkedConvert('confirm', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$BackupRestoreRequestToJson(
  BackupRestoreRequest instance,
) => <String, dynamic>{'confirm': instance.confirm};
