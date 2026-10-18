// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'reference_import_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReferenceImportPageCWProxy {
  ReferenceImportPage items(List<ReferenceImport> items);

  ReferenceImportPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceImportPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceImportPage(...).copyWith(id: 12, name: "My name")
  /// ```
  ReferenceImportPage call({List<ReferenceImport> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReferenceImportPage.copyWith(...)` or call `instanceOfReferenceImportPage.copyWith.fieldName(value)` for a single field.
class _$ReferenceImportPageCWProxyImpl implements _$ReferenceImportPageCWProxy {
  const _$ReferenceImportPageCWProxyImpl(this._value);

  final ReferenceImportPage _value;

  @override
  ReferenceImportPage items(List<ReferenceImport> items) => call(items: items);

  @override
  ReferenceImportPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceImportPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceImportPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReferenceImportPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ReferenceImportPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ReferenceImport>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ReferenceImportPageCopyWith on ReferenceImportPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReferenceImportPage.copyWith(...)` or `instanceOfReferenceImportPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReferenceImportPageCWProxy get copyWith =>
      _$ReferenceImportPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReferenceImportPage _$ReferenceImportPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReferenceImportPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = ReferenceImportPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => ReferenceImport.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ReferenceImportPageToJson(
  ReferenceImportPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
