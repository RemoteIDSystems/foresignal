// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'regional_index_def_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegionalIndexDefPageCWProxy {
  RegionalIndexDefPage items(List<RegionalIndexDef> items);

  RegionalIndexDefPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexDefPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexDefPage(...).copyWith(id: 12, name: "My name")
  /// ```
  RegionalIndexDefPage call({List<RegionalIndexDef> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegionalIndexDefPage.copyWith(...)` or call `instanceOfRegionalIndexDefPage.copyWith.fieldName(value)` for a single field.
class _$RegionalIndexDefPageCWProxyImpl
    implements _$RegionalIndexDefPageCWProxy {
  const _$RegionalIndexDefPageCWProxyImpl(this._value);

  final RegionalIndexDefPage _value;

  @override
  RegionalIndexDefPage items(List<RegionalIndexDef> items) =>
      call(items: items);

  @override
  RegionalIndexDefPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexDefPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexDefPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegionalIndexDefPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return RegionalIndexDefPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<RegionalIndexDef>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $RegionalIndexDefPageCopyWith on RegionalIndexDefPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegionalIndexDefPage.copyWith(...)` or `instanceOfRegionalIndexDefPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegionalIndexDefPageCWProxy get copyWith =>
      _$RegionalIndexDefPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegionalIndexDefPage _$RegionalIndexDefPageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RegionalIndexDefPage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items']);
  final val = RegionalIndexDefPage(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => RegionalIndexDef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$RegionalIndexDefPageToJson(
  RegionalIndexDefPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
