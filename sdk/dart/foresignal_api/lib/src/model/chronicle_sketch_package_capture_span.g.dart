// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_sketch_package_capture_span.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSketchPackageCaptureSpanCWProxy {
  ChronicleSketchPackageCaptureSpan from(String from);

  ChronicleSketchPackageCaptureSpan to(String? to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSketchPackageCaptureSpan(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSketchPackageCaptureSpan(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSketchPackageCaptureSpan call({String from, String? to});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSketchPackageCaptureSpan.copyWith(...)` or call `instanceOfChronicleSketchPackageCaptureSpan.copyWith.fieldName(value)` for a single field.
class _$ChronicleSketchPackageCaptureSpanCWProxyImpl
    implements _$ChronicleSketchPackageCaptureSpanCWProxy {
  const _$ChronicleSketchPackageCaptureSpanCWProxyImpl(this._value);

  final ChronicleSketchPackageCaptureSpan _value;

  @override
  ChronicleSketchPackageCaptureSpan from(String from) => call(from: from);

  @override
  ChronicleSketchPackageCaptureSpan to(String? to) => call(to: to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSketchPackageCaptureSpan(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSketchPackageCaptureSpan(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSketchPackageCaptureSpan call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSketchPackageCaptureSpan(
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as String,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as String?,
    );
  }
}

extension $ChronicleSketchPackageCaptureSpanCopyWith
    on ChronicleSketchPackageCaptureSpan {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSketchPackageCaptureSpan.copyWith(...)` or `instanceOfChronicleSketchPackageCaptureSpan.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSketchPackageCaptureSpanCWProxy get copyWith =>
      _$ChronicleSketchPackageCaptureSpanCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSketchPackageCaptureSpan _$ChronicleSketchPackageCaptureSpanFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleSketchPackageCaptureSpan', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['from']);
  final val = ChronicleSketchPackageCaptureSpan(
    from: $checkedConvert('from', (v) => v as String),
    to: $checkedConvert('to', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ChronicleSketchPackageCaptureSpanToJson(
  ChronicleSketchPackageCaptureSpan instance,
) => <String, dynamic>{'from': instance.from, 'to': ?instance.to};
