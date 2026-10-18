// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'access_request_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AccessRequestPageCWProxy {
  AccessRequestPage items(List<AccessRequest> items);

  AccessRequestPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AccessRequestPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AccessRequestPage(...).copyWith(id: 12, name: "My name")
  /// ```
  AccessRequestPage call({List<AccessRequest> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAccessRequestPage.copyWith(...)` or call `instanceOfAccessRequestPage.copyWith.fieldName(value)` for a single field.
class _$AccessRequestPageCWProxyImpl implements _$AccessRequestPageCWProxy {
  const _$AccessRequestPageCWProxyImpl(this._value);

  final AccessRequestPage _value;

  @override
  AccessRequestPage items(List<AccessRequest> items) => call(items: items);

  @override
  AccessRequestPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AccessRequestPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AccessRequestPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AccessRequestPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return AccessRequestPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<AccessRequest>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $AccessRequestPageCopyWith on AccessRequestPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAccessRequestPage.copyWith(...)` or `instanceOfAccessRequestPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AccessRequestPageCWProxy get copyWith =>
      _$AccessRequestPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AccessRequestPage _$AccessRequestPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AccessRequestPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = AccessRequestPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => AccessRequest.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AccessRequestPageToJson(AccessRequestPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': instance.nextCursor,
    };
