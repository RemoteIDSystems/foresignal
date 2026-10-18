// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_suggest_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSuggestRequestCWProxy {
  ChronicleSuggestRequest situationId(String? situationId);

  ChronicleSuggestRequest area(ChronicleArea? area);

  ChronicleSuggestRequest topic(String? topic);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSuggestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSuggestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSuggestRequest call({
    String? situationId,
    ChronicleArea? area,
    String? topic,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSuggestRequest.copyWith(...)` or call `instanceOfChronicleSuggestRequest.copyWith.fieldName(value)` for a single field.
class _$ChronicleSuggestRequestCWProxyImpl
    implements _$ChronicleSuggestRequestCWProxy {
  const _$ChronicleSuggestRequestCWProxyImpl(this._value);

  final ChronicleSuggestRequest _value;

  @override
  ChronicleSuggestRequest situationId(String? situationId) =>
      call(situationId: situationId);

  @override
  ChronicleSuggestRequest area(ChronicleArea? area) => call(area: area);

  @override
  ChronicleSuggestRequest topic(String? topic) => call(topic: topic);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSuggestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSuggestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSuggestRequest call({
    Object? situationId = const $CopyWithPlaceholder(),
    Object? area = const $CopyWithPlaceholder(),
    Object? topic = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSuggestRequest(
      situationId: situationId == const $CopyWithPlaceholder()
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String?,
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as ChronicleArea?,
      topic: topic == const $CopyWithPlaceholder()
          ? _value.topic
          // ignore: cast_nullable_to_non_nullable
          : topic as String?,
    );
  }
}

extension $ChronicleSuggestRequestCopyWith on ChronicleSuggestRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSuggestRequest.copyWith(...)` or `instanceOfChronicleSuggestRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSuggestRequestCWProxy get copyWith =>
      _$ChronicleSuggestRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSuggestRequest _$ChronicleSuggestRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleSuggestRequest', json, ($checkedConvert) {
  final val = ChronicleSuggestRequest(
    situationId: $checkedConvert('situationId', (v) => v as String?),
    area: $checkedConvert(
      'area',
      (v) =>
          v == null ? null : ChronicleArea.fromJson(v as Map<String, dynamic>),
    ),
    topic: $checkedConvert('topic', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ChronicleSuggestRequestToJson(
  ChronicleSuggestRequest instance,
) => <String, dynamic>{
  'situationId': ?instance.situationId,
  'area': ?instance.area?.toJson(),
  'topic': ?instance.topic,
};
