// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'archive_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ArchivePageCWProxy {
  ArchivePage items(List<Archive> items);

  ArchivePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchivePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchivePage(...).copyWith(id: 12, name: "My name")
  /// ```
  ArchivePage call({List<Archive> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfArchivePage.copyWith(...)` or call `instanceOfArchivePage.copyWith.fieldName(value)` for a single field.
class _$ArchivePageCWProxyImpl implements _$ArchivePageCWProxy {
  const _$ArchivePageCWProxyImpl(this._value);

  final ArchivePage _value;

  @override
  ArchivePage items(List<Archive> items) => call(items: items);

  @override
  ArchivePage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchivePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchivePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ArchivePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ArchivePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Archive>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ArchivePageCopyWith on ArchivePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfArchivePage.copyWith(...)` or `instanceOfArchivePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ArchivePageCWProxy get copyWith => _$ArchivePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ArchivePage _$ArchivePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ArchivePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = ArchivePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Archive.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ArchivePageToJson(ArchivePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
