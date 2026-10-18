// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'canonical_event_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CanonicalEventPageCWProxy {
  CanonicalEventPage items(List<CanonicalEvent> items);

  CanonicalEventPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CanonicalEventPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CanonicalEventPage(...).copyWith(id: 12, name: "My name")
  /// ```
  CanonicalEventPage call({List<CanonicalEvent> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCanonicalEventPage.copyWith(...)` or call `instanceOfCanonicalEventPage.copyWith.fieldName(value)` for a single field.
class _$CanonicalEventPageCWProxyImpl implements _$CanonicalEventPageCWProxy {
  const _$CanonicalEventPageCWProxyImpl(this._value);

  final CanonicalEventPage _value;

  @override
  CanonicalEventPage items(List<CanonicalEvent> items) => call(items: items);

  @override
  CanonicalEventPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CanonicalEventPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CanonicalEventPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CanonicalEventPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return CanonicalEventPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<CanonicalEvent>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $CanonicalEventPageCopyWith on CanonicalEventPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCanonicalEventPage.copyWith(...)` or `instanceOfCanonicalEventPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CanonicalEventPageCWProxy get copyWith =>
      _$CanonicalEventPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CanonicalEventPage _$CanonicalEventPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CanonicalEventPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = CanonicalEventPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => CanonicalEvent.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$CanonicalEventPageToJson(CanonicalEventPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
