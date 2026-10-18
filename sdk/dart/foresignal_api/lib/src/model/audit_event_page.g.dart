// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'audit_event_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AuditEventPageCWProxy {
  AuditEventPage items(List<AuditEvent> items);

  AuditEventPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AuditEventPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AuditEventPage(...).copyWith(id: 12, name: "My name")
  /// ```
  AuditEventPage call({List<AuditEvent> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAuditEventPage.copyWith(...)` or call `instanceOfAuditEventPage.copyWith.fieldName(value)` for a single field.
class _$AuditEventPageCWProxyImpl implements _$AuditEventPageCWProxy {
  const _$AuditEventPageCWProxyImpl(this._value);

  final AuditEventPage _value;

  @override
  AuditEventPage items(List<AuditEvent> items) => call(items: items);

  @override
  AuditEventPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AuditEventPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AuditEventPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AuditEventPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return AuditEventPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<AuditEvent>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $AuditEventPageCopyWith on AuditEventPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAuditEventPage.copyWith(...)` or `instanceOfAuditEventPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AuditEventPageCWProxy get copyWith => _$AuditEventPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AuditEventPage _$AuditEventPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AuditEventPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = AuditEventPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => AuditEvent.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AuditEventPageToJson(AuditEventPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
