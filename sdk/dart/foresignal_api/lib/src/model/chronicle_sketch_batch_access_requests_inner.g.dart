// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_sketch_batch_access_requests_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSketchBatchAccessRequestsInnerCWProxy {
  ChronicleSketchBatchAccessRequestsInner requestId(String requestId);

  ChronicleSketchBatchAccessRequestsInner linkId(String? linkId);

  ChronicleSketchBatchAccessRequestsInner sketchId(String sketchId);

  ChronicleSketchBatchAccessRequestsInner requesterHandle(
    String requesterHandle,
  );

  ChronicleSketchBatchAccessRequestsInner message(String? message);

  ChronicleSketchBatchAccessRequestsInner offerShare(bool? offerShare);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSketchBatchAccessRequestsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSketchBatchAccessRequestsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSketchBatchAccessRequestsInner call({
    String requestId,
    String? linkId,
    String sketchId,
    String requesterHandle,
    String? message,
    bool? offerShare,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSketchBatchAccessRequestsInner.copyWith(...)` or call `instanceOfChronicleSketchBatchAccessRequestsInner.copyWith.fieldName(value)` for a single field.
class _$ChronicleSketchBatchAccessRequestsInnerCWProxyImpl
    implements _$ChronicleSketchBatchAccessRequestsInnerCWProxy {
  const _$ChronicleSketchBatchAccessRequestsInnerCWProxyImpl(this._value);

  final ChronicleSketchBatchAccessRequestsInner _value;

  @override
  ChronicleSketchBatchAccessRequestsInner requestId(String requestId) =>
      call(requestId: requestId);

  @override
  ChronicleSketchBatchAccessRequestsInner linkId(String? linkId) =>
      call(linkId: linkId);

  @override
  ChronicleSketchBatchAccessRequestsInner sketchId(String sketchId) =>
      call(sketchId: sketchId);

  @override
  ChronicleSketchBatchAccessRequestsInner requesterHandle(
    String requesterHandle,
  ) => call(requesterHandle: requesterHandle);

  @override
  ChronicleSketchBatchAccessRequestsInner message(String? message) =>
      call(message: message);

  @override
  ChronicleSketchBatchAccessRequestsInner offerShare(bool? offerShare) =>
      call(offerShare: offerShare);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSketchBatchAccessRequestsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSketchBatchAccessRequestsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSketchBatchAccessRequestsInner call({
    Object? requestId = const $CopyWithPlaceholder(),
    Object? linkId = const $CopyWithPlaceholder(),
    Object? sketchId = const $CopyWithPlaceholder(),
    Object? requesterHandle = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? offerShare = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSketchBatchAccessRequestsInner(
      requestId: requestId == const $CopyWithPlaceholder() || requestId == null
          ? _value.requestId
          // ignore: cast_nullable_to_non_nullable
          : requestId as String,
      linkId: linkId == const $CopyWithPlaceholder()
          ? _value.linkId
          // ignore: cast_nullable_to_non_nullable
          : linkId as String?,
      sketchId: sketchId == const $CopyWithPlaceholder() || sketchId == null
          ? _value.sketchId
          // ignore: cast_nullable_to_non_nullable
          : sketchId as String,
      requesterHandle:
          requesterHandle == const $CopyWithPlaceholder() ||
              requesterHandle == null
          ? _value.requesterHandle
          // ignore: cast_nullable_to_non_nullable
          : requesterHandle as String,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      offerShare: offerShare == const $CopyWithPlaceholder()
          ? _value.offerShare
          // ignore: cast_nullable_to_non_nullable
          : offerShare as bool?,
    );
  }
}

extension $ChronicleSketchBatchAccessRequestsInnerCopyWith
    on ChronicleSketchBatchAccessRequestsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSketchBatchAccessRequestsInner.copyWith(...)` or `instanceOfChronicleSketchBatchAccessRequestsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSketchBatchAccessRequestsInnerCWProxy get copyWith =>
      _$ChronicleSketchBatchAccessRequestsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSketchBatchAccessRequestsInner
_$ChronicleSketchBatchAccessRequestsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleSketchBatchAccessRequestsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(
        json,
        requiredKeys: const ['requestId', 'sketchId', 'requesterHandle'],
      );
      final val = ChronicleSketchBatchAccessRequestsInner(
        requestId: $checkedConvert('requestId', (v) => v as String),
        linkId: $checkedConvert('linkId', (v) => v as String?),
        sketchId: $checkedConvert('sketchId', (v) => v as String),
        requesterHandle: $checkedConvert('requesterHandle', (v) => v as String),
        message: $checkedConvert('message', (v) => v as String?),
        offerShare: $checkedConvert('offerShare', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleSketchBatchAccessRequestsInnerToJson(
  ChronicleSketchBatchAccessRequestsInner instance,
) => <String, dynamic>{
  'requestId': instance.requestId,
  'linkId': ?instance.linkId,
  'sketchId': instance.sketchId,
  'requesterHandle': instance.requesterHandle,
  'message': ?instance.message,
  'offerShare': ?instance.offerShare,
};
