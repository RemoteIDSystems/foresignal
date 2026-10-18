// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'received_package_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReceivedPackagePageCWProxy {
  ReceivedPackagePage items(List<ReceivedPackage> items);

  ReceivedPackagePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReceivedPackagePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReceivedPackagePage(...).copyWith(id: 12, name: "My name")
  /// ```
  ReceivedPackagePage call({List<ReceivedPackage> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReceivedPackagePage.copyWith(...)` or call `instanceOfReceivedPackagePage.copyWith.fieldName(value)` for a single field.
class _$ReceivedPackagePageCWProxyImpl implements _$ReceivedPackagePageCWProxy {
  const _$ReceivedPackagePageCWProxyImpl(this._value);

  final ReceivedPackagePage _value;

  @override
  ReceivedPackagePage items(List<ReceivedPackage> items) => call(items: items);

  @override
  ReceivedPackagePage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReceivedPackagePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReceivedPackagePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReceivedPackagePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ReceivedPackagePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ReceivedPackage>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ReceivedPackagePageCopyWith on ReceivedPackagePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReceivedPackagePage.copyWith(...)` or `instanceOfReceivedPackagePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReceivedPackagePageCWProxy get copyWith =>
      _$ReceivedPackagePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReceivedPackagePage _$ReceivedPackagePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReceivedPackagePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = ReceivedPackagePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => ReceivedPackage.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ReceivedPackagePageToJson(
  ReceivedPackagePage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': instance.nextCursor,
};
