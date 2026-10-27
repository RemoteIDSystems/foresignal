// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'backup_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BackupPageCWProxy {
  BackupPage items(List<Backup> items);

  BackupPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BackupPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BackupPage(...).copyWith(id: 12, name: "My name")
  /// ```
  BackupPage call({List<Backup> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBackupPage.copyWith(...)` or call `instanceOfBackupPage.copyWith.fieldName(value)` for a single field.
class _$BackupPageCWProxyImpl implements _$BackupPageCWProxy {
  const _$BackupPageCWProxyImpl(this._value);

  final BackupPage _value;

  @override
  BackupPage items(List<Backup> items) => call(items: items);

  @override
  BackupPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BackupPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BackupPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BackupPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return BackupPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Backup>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $BackupPageCopyWith on BackupPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBackupPage.copyWith(...)` or `instanceOfBackupPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BackupPageCWProxy get copyWith => _$BackupPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BackupPage _$BackupPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BackupPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = BackupPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Backup.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$BackupPageToJson(BackupPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
