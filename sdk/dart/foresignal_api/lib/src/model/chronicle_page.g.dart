// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChroniclePageCWProxy {
  ChroniclePage items(List<Chronicle> items);

  ChroniclePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChroniclePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChroniclePage(...).copyWith(id: 12, name: "My name")
  /// ```
  ChroniclePage call({List<Chronicle> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChroniclePage.copyWith(...)` or call `instanceOfChroniclePage.copyWith.fieldName(value)` for a single field.
class _$ChroniclePageCWProxyImpl implements _$ChroniclePageCWProxy {
  const _$ChroniclePageCWProxyImpl(this._value);

  final ChroniclePage _value;

  @override
  ChroniclePage items(List<Chronicle> items) => call(items: items);

  @override
  ChroniclePage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChroniclePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChroniclePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChroniclePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ChroniclePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Chronicle>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ChroniclePageCopyWith on ChroniclePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChroniclePage.copyWith(...)` or `instanceOfChroniclePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChroniclePageCWProxy get copyWith => _$ChroniclePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChroniclePage _$ChroniclePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChroniclePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = ChroniclePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Chronicle.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ChroniclePageToJson(ChroniclePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': instance.nextCursor,
    };
