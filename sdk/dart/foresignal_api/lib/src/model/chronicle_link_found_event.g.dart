// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_link_found_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLinkFoundEventCWProxy {
  ChronicleLinkFoundEvent chronicleId(String chronicleId);

  ChronicleLinkFoundEvent link(ChronicleRelatedPageItemsInner link);

  ChronicleLinkFoundEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLinkFoundEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLinkFoundEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLinkFoundEvent call({
    String chronicleId,
    ChronicleRelatedPageItemsInner link,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLinkFoundEvent.copyWith(...)` or call `instanceOfChronicleLinkFoundEvent.copyWith.fieldName(value)` for a single field.
class _$ChronicleLinkFoundEventCWProxyImpl
    implements _$ChronicleLinkFoundEventCWProxy {
  const _$ChronicleLinkFoundEventCWProxyImpl(this._value);

  final ChronicleLinkFoundEvent _value;

  @override
  ChronicleLinkFoundEvent chronicleId(String chronicleId) =>
      call(chronicleId: chronicleId);

  @override
  ChronicleLinkFoundEvent link(ChronicleRelatedPageItemsInner link) =>
      call(link: link);

  @override
  ChronicleLinkFoundEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLinkFoundEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLinkFoundEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLinkFoundEvent call({
    Object? chronicleId = const $CopyWithPlaceholder(),
    Object? link = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLinkFoundEvent(
      chronicleId:
          chronicleId == const $CopyWithPlaceholder() || chronicleId == null
          ? _value.chronicleId
          // ignore: cast_nullable_to_non_nullable
          : chronicleId as String,
      link: link == const $CopyWithPlaceholder() || link == null
          ? _value.link
          // ignore: cast_nullable_to_non_nullable
          : link as ChronicleRelatedPageItemsInner,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $ChronicleLinkFoundEventCopyWith on ChronicleLinkFoundEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLinkFoundEvent.copyWith(...)` or `instanceOfChronicleLinkFoundEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLinkFoundEventCWProxy get copyWith =>
      _$ChronicleLinkFoundEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLinkFoundEvent _$ChronicleLinkFoundEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleLinkFoundEvent', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['chronicleId', 'link', 'at']);
  final val = ChronicleLinkFoundEvent(
    chronicleId: $checkedConvert('chronicleId', (v) => v as String),
    link: $checkedConvert(
      'link',
      (v) => ChronicleRelatedPageItemsInner.fromJson(v as Map<String, dynamic>),
    ),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$ChronicleLinkFoundEventToJson(
  ChronicleLinkFoundEvent instance,
) => <String, dynamic>{
  'chronicleId': instance.chronicleId,
  'link': instance.link.toJson(),
  'at': instance.at.toIso8601String(),
};
