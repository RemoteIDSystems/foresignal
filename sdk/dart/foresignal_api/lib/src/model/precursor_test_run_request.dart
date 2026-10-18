//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_test_run_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorTestRunRequest {
  /// Returns a new [PrecursorTestRunRequest] instance.
  PrecursorTestRunRequest({

     this.from,

     this.to,

     this.fromSeq,

     this.toSeq,
  });

  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;



  @JsonKey(
    
    name: r'fromSeq',
    required: false,
    includeIfNull: false,
  )


  final int? fromSeq;



  @JsonKey(
    
    name: r'toSeq',
    required: false,
    includeIfNull: false,
  )


  final int? toSeq;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorTestRunRequest &&
      other.from == from &&
      other.to == to &&
      other.fromSeq == fromSeq &&
      other.toSeq == toSeq;

    @override
    int get hashCode =>
        from.hashCode +
        to.hashCode +
        fromSeq.hashCode +
        toSeq.hashCode;

  factory PrecursorTestRunRequest.fromJson(Map<String, dynamic> json) => _$PrecursorTestRunRequestFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorTestRunRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

