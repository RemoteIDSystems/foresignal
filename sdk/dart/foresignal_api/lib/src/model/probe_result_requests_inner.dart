//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'probe_result_requests_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProbeResultRequestsInner {
  /// Returns a new [ProbeResultRequestsInner] instance.
  ProbeResultRequestsInner({

     this.method,

     this.url,

     this.status,

     this.durationMs,

     this.contentType,

     this.bytes,

     this.truncated,

     this.body,
  });

  @JsonKey(
    
    name: r'method',
    required: false,
    includeIfNull: false,
  )


  final String? method;



      /// Credentials redacted
  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final int? status;



  @JsonKey(
    
    name: r'durationMs',
    required: false,
    includeIfNull: false,
  )


  final int? durationMs;



  @JsonKey(
    
    name: r'contentType',
    required: false,
    includeIfNull: false,
  )


  final String? contentType;



  @JsonKey(
    
    name: r'bytes',
    required: false,
    includeIfNull: false,
  )


  final int? bytes;



  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;



  @JsonKey(
    
    name: r'body',
    required: false,
    includeIfNull: false,
  )


  final String? body;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProbeResultRequestsInner &&
      other.method == method &&
      other.url == url &&
      other.status == status &&
      other.durationMs == durationMs &&
      other.contentType == contentType &&
      other.bytes == bytes &&
      other.truncated == truncated &&
      other.body == body;

    @override
    int get hashCode =>
        method.hashCode +
        url.hashCode +
        status.hashCode +
        durationMs.hashCode +
        contentType.hashCode +
        bytes.hashCode +
        truncated.hashCode +
        body.hashCode;

  factory ProbeResultRequestsInner.fromJson(Map<String, dynamic> json) => _$ProbeResultRequestsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ProbeResultRequestsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

