// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'peer_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PeerPageCWProxy {
  PeerPage items(List<Peer> items);

  PeerPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PeerPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PeerPage(...).copyWith(id: 12, name: "My name")
  /// ```
  PeerPage call({List<Peer> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPeerPage.copyWith(...)` or call `instanceOfPeerPage.copyWith.fieldName(value)` for a single field.
class _$PeerPageCWProxyImpl implements _$PeerPageCWProxy {
  const _$PeerPageCWProxyImpl(this._value);

  final PeerPage _value;

  @override
  PeerPage items(List<Peer> items) => call(items: items);

  @override
  PeerPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PeerPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PeerPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PeerPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return PeerPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Peer>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $PeerPageCopyWith on PeerPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPeerPage.copyWith(...)` or `instanceOfPeerPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PeerPageCWProxy get copyWith => _$PeerPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PeerPage _$PeerPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PeerPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = PeerPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Peer.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PeerPageToJson(PeerPage instance) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
