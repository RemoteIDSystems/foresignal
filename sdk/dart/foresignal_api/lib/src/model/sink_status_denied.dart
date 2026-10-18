//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sink_status_denied.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SinkStatusDenied {
  /// Returns a new [SinkStatusDenied] instance.
  SinkStatusDenied({

    required  this.total,

     this.byReason,

     this.bySource,
  });

  @JsonKey(
    
    name: r'total',
    required: true,
    includeIfNull: false,
  )


  final int total;



      /// e.g. licence-forbidden, licence-local-only, private
  @JsonKey(
    
    name: r'byReason',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? byReason;



  @JsonKey(
    
    name: r'bySource',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? bySource;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SinkStatusDenied &&
      other.total == total &&
      other.byReason == byReason &&
      other.bySource == bySource;

    @override
    int get hashCode =>
        total.hashCode +
        byReason.hashCode +
        bySource.hashCode;

  factory SinkStatusDenied.fromJson(Map<String, dynamic> json) => _$SinkStatusDeniedFromJson(json);

  Map<String, dynamic> toJson() => _$SinkStatusDeniedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

