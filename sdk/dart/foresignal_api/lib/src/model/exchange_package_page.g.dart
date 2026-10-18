// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exchange_package_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExchangePackagePageCWProxy {
  ExchangePackagePage items(List<ExchangePackage> items);

  ExchangePackagePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExchangePackagePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExchangePackagePage(...).copyWith(id: 12, name: "My name")
  /// ```
  ExchangePackagePage call({List<ExchangePackage> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExchangePackagePage.copyWith(...)` or call `instanceOfExchangePackagePage.copyWith.fieldName(value)` for a single field.
class _$ExchangePackagePageCWProxyImpl implements _$ExchangePackagePageCWProxy {
  const _$ExchangePackagePageCWProxyImpl(this._value);

  final ExchangePackagePage _value;

  @override
  ExchangePackagePage items(List<ExchangePackage> items) => call(items: items);

  @override
  ExchangePackagePage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExchangePackagePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExchangePackagePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExchangePackagePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ExchangePackagePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ExchangePackage>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ExchangePackagePageCopyWith on ExchangePackagePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExchangePackagePage.copyWith(...)` or `instanceOfExchangePackagePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExchangePackagePageCWProxy get copyWith =>
      _$ExchangePackagePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExchangePackagePage _$ExchangePackagePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExchangePackagePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = ExchangePackagePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => ExchangePackage.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ExchangePackagePageToJson(
  ExchangePackagePage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': instance.nextCursor,
};
